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
package io.dts.server.handler.support;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.api.DtsServerMessageSender;
import io.dts.common.common.Constants;
import io.dts.common.common.DtsXID;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BranchCommitMessage;
import io.dts.common.protocol.header.BranchCommitResultMessage;
import io.dts.common.protocol.header.BranchRollBackMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.server.network.DtsServerContainer;
import io.dts.server.store.DtsLogDao;
import io.dts.server.store.DtsTransStatusDao;
import io.dts.server.struct.BranchLog;
import io.dts.server.struct.GlobalLog;
import io.dts.server.struct.GlobalTransactionState;

/**
 * @author liushiming
 * @version ClientMessageHandler.java, v 0.0.1 2017年9月18日 下午5:38:29 liushiming
 */
public interface ClientMessageHandler {


  String processMessage(BeginMessage beginMessage, String clientIp);

  void processMessage(GlobalCommitMessage globalCommitMessage);

  void processMessage(GlobalRollbackMessage globalRollbackMessage);


  public static ClientMessageHandler createClientMessageProcessor(
      DtsTransStatusDao dtsTransStatusDao, DtsLogDao dtsLogDao,
      DtsServerMessageSender serverMessageServer) {

    return new ClientMessageHandler() {
      private final Logger logger = LoggerFactory.getLogger(RmMessageHandler.class);

      private final SyncRmMessagHandler globalResultMessageHandler =
          SyncRmMessagHandler.createSyncGlobalResultProcess(dtsTransStatusDao, dtsLogDao);


      // 开始一个事务
      @Override
      public String processMessage(BeginMessage beginMessage, String clientIp) {
        GlobalLog globalLog = new GlobalLog();
        globalLog.setState(GlobalTransactionState.Begin.getValue());
        globalLog.setTimeout(beginMessage.getTimeout());
        globalLog.setClientAppName(clientIp);
        globalLog.setContainPhase2CommitBranch(false);
        dtsLogDao.insertGlobalLog(globalLog, DtsServerContainer.mid);;
        long tranId = globalLog.getTransId();
        dtsTransStatusDao.saveGlobalLog(tranId, globalLog, beginMessage.getTimeout());
        String xid = DtsXID.generateXID(tranId);
        return xid;
      }

      // 事务提交
      @Override
      public void processMessage(GlobalCommitMessage globalCommitMessage) {
        Long tranId = globalCommitMessage.getTranId();
        GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
        if (globalLog == null) {
          throw new DtsException("transaction doesn't exist.");
        } else {
          switch (GlobalTransactionState.parse(globalLog.getState())) {
            case Begin:
              List<BranchLog> branchLogs =
                  dtsTransStatusDao.queryBranchLogByTransId(globalLog.getTransId());
              if (branchLogs.size() == 0) {
                dtsLogDao.deleteGlobalLog(globalLog.getTransId(), DtsServerContainer.mid);
                dtsTransStatusDao.removeGlobalLog(tranId);
                return;
              }
              // 通知各个分支开始提交
              try {
                this.syncGlobalCommit(branchLogs, globalLog.getTransId());
                globalLog.setState(GlobalTransactionState.Committed.getValue());
                dtsLogDao.updateGlobalLog(globalLog, DtsServerContainer.mid);
              } catch (Exception e) {
                logger.error(e.getMessage(), e);
                globalLog.setState(GlobalTransactionState.InDoubt.getValue());
                dtsLogDao.updateGlobalLog(globalLog, DtsServerContainer.mid);
                throw new DtsException("notify resourcemanager to commit failed");
              }
              return;
            default:
              throw new DtsException("Unknown state " + globalLog.getState());
          }

        }
      }

      // 事务回滚
      @Override
      public void processMessage(GlobalRollbackMessage globalRollbackMessage) {
        long tranId = globalRollbackMessage.getTranId();
        GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
        if (globalLog == null) {
          throw new DtsException("transaction doesn't exist.");
        } else {
          switch (GlobalTransactionState.parse(globalLog.getState())) {
            case Begin:
              List<BranchLog> branchLogs =
                  dtsTransStatusDao.queryBranchLogByTransId(globalLog.getTransId());
              // 通知各个分支开始回滚
              try {
                this.syncGlobalRollback(branchLogs, globalLog.getTransId());
                globalLog.setState(GlobalTransactionState.Rollbacked.getValue());
                dtsLogDao.updateGlobalLog(globalLog, DtsServerContainer.mid);
              } catch (Exception e) {
                logger.error(e.getMessage(), e);
                globalLog.setState(GlobalTransactionState.InDoubt.getValue());
                dtsLogDao.updateGlobalLog(globalLog, DtsServerContainer.mid);
                throw new DtsException("notify resourcemanager to commit failed");
              }
              return;
            default:
              throw new DtsException("Unknown state " + globalLog.getState());
          }
        }
      }

      protected void syncGlobalCommit(List<BranchLog> branchLogs, long transId) {
        for (BranchLog branchLog : branchLogs) {
          String clientAddress = branchLog.getClientIp();
          Long branchId = branchLog.getBranchId();
          BranchCommitMessage branchCommitMessage = new BranchCommitMessage();
          branchCommitMessage.setServerAddr(DtsXID.getSvrAddr());
          branchCommitMessage.setTranId(transId);
          branchCommitMessage.setBranchId(branchId);
          branchCommitMessage.setUdata(branchLog.getUdata());
          branchCommitMessage.setRetrySql(branchLog.getRetrySql());
          branchCommitMessage.setDbName(branchLog.getClientInfo());
          BranchCommitResultMessage branchCommitResult = null;
          try {
            branchCommitResult = serverMessageServer.invokeSync(clientAddress, branchCommitMessage,
                Constants.RPC_INVOKE_TIMEOUT);
          } catch (DtsException e) {
            dtsLogDao.insertBranchErrorLog(branchLog, DtsServerContainer.mid);
            logger.error(
                "notify " + clientAddress + " to commit occur system error,branchId:" + branchId,
                e);
          }
          if (branchCommitResult != null)
            globalResultMessageHandler.processMessage(clientAddress, branchCommitResult);
        }

      }

      protected void syncGlobalRollback(List<BranchLog> branchLogs, long transId) {
        for (int i = 0; i < branchLogs.size(); i++) {
          BranchLog branchLog = branchLogs.get(i);
          Long branchId = branchLog.getBranchId();
          String clientAddress = branchLog.getClientIp();
          BranchRollBackMessage branchRollbackMessage = new BranchRollBackMessage();
          branchRollbackMessage.setServerAddr(DtsXID.getSvrAddr());
          branchRollbackMessage.setTranId(transId);
          branchRollbackMessage.setBranchId(branchId);
          branchRollbackMessage.setDbName(branchLog.getClientInfo());
          branchRollbackMessage.setUdata(branchLog.getUdata());
          BranchRollbackResultMessage branchRollbackResult = null;
          try {
            branchRollbackResult = serverMessageServer.invokeSync(clientAddress,
                branchRollbackMessage, Constants.RPC_INVOKE_TIMEOUT);
          } catch (DtsException e) {
            dtsLogDao.insertBranchErrorLog(branchLog, DtsServerContainer.mid);
            logger.error(
                "notify " + clientAddress + " rollback occur system error,branchId:" + branchId, e);
          }
          if (branchRollbackResult != null)
            globalResultMessageHandler.processMessage(clientAddress, branchRollbackResult);
        }
      }
    };
  }

}
