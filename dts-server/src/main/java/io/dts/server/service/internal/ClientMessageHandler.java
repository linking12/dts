/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.dts.server.service.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.api.DtsServerMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BranchCommitMessage;
import io.dts.common.protocol.header.BranchCommitResultMessage;
import io.dts.common.protocol.header.BranchRollBackMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.server.model.BranchLog;
import io.dts.server.model.GlobalLog;
import io.dts.server.model.GlobalTransactionState;
import io.dts.server.service.CommitingResultCode;
import io.dts.server.service.RollbackingResultCode;
import io.dts.server.store.DtsLogDao;
import io.dts.server.store.DtsTransStatusDao;

/**
 * @author liushiming
 * @version ClientMessageHandler.java, v 0.0.1 2017年9月18日 下午5:38:29 liushiming
 */
public interface ClientMessageHandler {


  String processMessage(BeginMessage beginMessage, String clientIp);

  void processMessage(GlobalCommitMessage globalCommitMessage, String clientIp);

  void processMessage(GlobalRollbackMessage globalRollbackMessage, String clientIp);


  public static ClientMessageHandler createClientMessageProcessor(
      DtsTransStatusDao dtsTransStatusDao, DtsLogDao dtsLogDao,
      DtsServerMessageSender serverMessageServer) {

    return new ClientMessageHandler() {
      private final Logger logger = LoggerFactory.getLogger(ResourceManagerMessageHandler.class);

      private final SyncGlobalResultMessagHandler globalResultMessageHandler =
          SyncGlobalResultMessagHandler.createSyncGlobalResultProcess(dtsTransStatusDao, dtsLogDao);

      // 开始一个事务
      @Override
      public String processMessage(BeginMessage beginMessage, String clientIp) {
        GlobalLog globalLog = new GlobalLog();
        globalLog.setState(GlobalTransactionState.Begin.getValue());
        globalLog.setTimeout(beginMessage.getTimeout());
        globalLog.setClientAppName(clientIp);
        globalLog.setContainPhase2CommitBranch(false);
        dtsLogDao.insertGlobalLog(globalLog, 1);;
        long tranId = globalLog.getTransId();
        dtsTransStatusDao.insertGlobalLog(tranId, globalLog);
        String xid = TxcXID.generateXID(tranId);
        return xid;
      }

      // 事务提交
      @Override
      public void processMessage(GlobalCommitMessage globalCommitMessage, String clientIp) {
        Long tranId = globalCommitMessage.getTranId();
        GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
        if (globalLog == null) {
          // 事务已超时
          if (dtsTransStatusDao.queryTimeOut(tranId)) {
            dtsTransStatusDao.removeTimeOut(tranId);
            throw new DtsException(
                "transaction doesn't exist. It has been rollbacked because of timeout.");
          } // 在本地缓存未查到事务
          else {
            throw new DtsException("transaction doesn't exist.");
          }
        } else {
          switch (GlobalTransactionState.parse(globalLog.getState())) {
            case Committing:
              if (!globalLog.isContainPhase2CommitBranch()) {
                return;
              } else {
                throw new DtsException("transaction is committing.");
              }
            case Rollbacking:
              if (dtsTransStatusDao.queryTimeOut(tranId)) {
                dtsTransStatusDao.removeTimeOut(tranId);
                throw new DtsException("transaction is rollbacking because of timeout.");
              } else {
                throw new DtsException("transaction is rollbacking.");
              }
            case Begin:
            case CommitHeuristic:
              List<BranchLog> branchLogs = dtsTransStatusDao
                  .queryBranchLogByTransId(globalLog.getTransId(), false, false, false);
              BranchLog.setLastBranchDelTrxKey(branchLogs);
              if (branchLogs.size() == 0) {
                dtsLogDao.deleteGlobalLog(globalLog.getTransId(), 1);
                dtsTransStatusDao.clearGlobalLog(tranId);
                return;
              }
              globalLog.setState(GlobalTransactionState.Committing.getValue());
              try {
                dtsLogDao.updateGlobalLog(globalLog, 1);
              } catch (Exception e) {
                logger.error(e.getMessage(), e);
                // 状态设置为提交未决
                globalLog.setState(GlobalTransactionState.CommitHeuristic.getValue());
                throw new DtsException("update global status fail.");
              }
              if (!globalLog.isContainPhase2CommitBranch()) {
                for (BranchLog branchLog : branchLogs) {
                  dtsTransStatusDao.insertCommitedResult(branchLog.getBranchId(),
                      CommitingResultCode.BEGIN.getValue());
                }
                this.syncGlobalCommit(branchLogs, globalLog);

              } else {
                try {
                  Collections.sort(branchLogs, new Comparator<BranchLog>() {
                    @Override
                    public int compare(BranchLog o1, BranchLog o2) {
                      return (int) (o1.getBranchId() - o2.getBranchId());
                    }
                  });
                  this.syncGlobalCommit(branchLogs, globalLog);
                } catch (Exception e) {
                  logger.error(e.getMessage(), e);
                  throw new DtsException("notify resourcemanager to commit failed");
                }
              }
              return;
            default:
              throw new DtsException("Unknown state " + globalLog.getState());
          }

        }
      }

      // 事务回滚
      @Override
      public void processMessage(GlobalRollbackMessage globalRollbackMessage, String clientIp) {
        long tranId = globalRollbackMessage.getTranId();
        GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
        if (globalLog == null) {
          if (dtsTransStatusDao.queryTimeOut(tranId)) {
            dtsTransStatusDao.removeTimeOut(tranId);
            throw new DtsException(
                "transaction doesn't exist. It has been rollbacked because of timeout.");
          } else {
            throw new DtsException("transaction doesn't exist.");
          }
        } else if (globalLog.getState() == GlobalTransactionState.Committing.getValue()) {
          throw new DtsException("transaction is committing.");
        } else if (globalLog.getState() == GlobalTransactionState.Rollbacking.getValue()) {
          if (dtsTransStatusDao.queryTimeOut(tranId)) {
            dtsTransStatusDao.removeTimeOut(tranId);
            throw new DtsException("transaction has been rollbacking because of timeout.");
          } else {
            throw new DtsException("transaction has been rollbacking.");
          }
        } else if (globalLog.getState() == GlobalTransactionState.Begin.getValue()) {
          List<BranchLog> branchLogs =
              dtsTransStatusDao.queryBranchLogByTransId(globalLog.getTransId(), true, true, false);
          BranchLog.setLastBranchDelTrxKey(branchLogs);
          globalLog.setState(GlobalTransactionState.Rollbacking.getValue());
          try {
            if (globalRollbackMessage.getRealSvrAddr() == null)
              dtsLogDao.updateGlobalLog(globalLog, 1);
          } catch (Exception e) {
            globalLog.setState(GlobalTransactionState.Begin.getValue());
            throw new DtsException("update global status fail.");
          }
          try {
            String clusterNode = globalRollbackMessage.getRealSvrAddr();
            if (clusterNode == null)
              this.syncGlobalRollback(branchLogs, globalLog);
            // handler.globalRollbackForPrevNode(branchLogs, globalLog, tranId, clusterNode);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DtsException("notify resourcemanager to rollback failed");
          }
        } else {
          throw new DtsException("Unknown state " + globalLog.getState());
        }
      }

      protected void syncGlobalCommit(List<BranchLog> branchLogs, GlobalLog globalLog) {

        branchLogs.forEach(branchLog -> {
          String clientAddress = branchLog.getClientIp();
          Long branchId = branchLog.getBranchId();
          BranchCommitMessage branchCommitMessage = new BranchCommitMessage();
          branchCommitMessage.setServerAddr(TxcXID.getSvrAddr());
          branchCommitMessage.setTranId(globalLog.getTransId());
          branchCommitMessage.setBranchId(branchId);
          branchCommitMessage.setUdata(branchLog.getUdata());
          branchCommitMessage.setCommitMode(branchLog.getCommitMode());
          branchCommitMessage.setRetrySql(branchLog.getRetrySql());
          branchCommitMessage.setDbName(branchLog.getClientInfo());
          try {
            BranchCommitResultMessage branchCommitResult =
                serverMessageServer.invokeSync(clientAddress, branchCommitMessage, 3000);
            if (branchCommitResult == null) {
              dtsTransStatusDao.insertCommitedResult(branchId,
                  CommitingResultCode.TIMEOUT.getValue());
            } else {
              globalResultMessageHandler.processMessage(clientAddress, branchCommitResult);
            }
          } catch (DtsException e) {
            logger.error(e.getMessage(), e);
            dtsTransStatusDao.insertCommitedResult(branchId,
                CommitingResultCode.TIMEOUT.getValue());
          }

        });
      }

      protected void syncGlobalRollback(List<BranchLog> branchLogs, GlobalLog globalLog) {
        branchLogs.forEach(branchLog -> {
          String clientAddress = branchLog.getClientIp();
          Long branchId = branchLog.getBranchId();
          BranchRollBackMessage branchRollbackMessage = new BranchRollBackMessage();
          branchRollbackMessage.setServerAddr(TxcXID.getSvrAddr());
          branchRollbackMessage.setTranId(globalLog.getTransId());
          branchRollbackMessage.setBranchId(branchId);
          branchRollbackMessage.setUdata(branchLog.getUdata());
          branchRollbackMessage.setCommitMode((byte) branchLog.getCommitMode());
          branchRollbackMessage.setIsDelLock((byte) branchLog.getIsDelLock());
          try {
            BranchRollbackResultMessage branchRollbackResult =
                serverMessageServer.invokeSync(clientAddress, branchRollbackMessage, 3000);
            if (branchRollbackResult == null) {
              dtsTransStatusDao.insertRollbackResult(branchId,
                  RollbackingResultCode.TIMEOUT.getValue());
            } else {
              globalResultMessageHandler.processMessage(clientAddress, branchRollbackResult);
            }
          } catch (DtsException e) {
            logger.error(e.getMessage(), e);
            dtsTransStatusDao.insertRollbackResult(branchId,
                RollbackingResultCode.TIMEOUT.getValue());
          }

        });
      }

    };
  }

}
