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
package io.dts.server.handler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.dts.common.common.TxcXID;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.server.exception.DtsBizException;
import io.dts.server.model.BranchLog;
import io.dts.server.model.BranchTransactionState;
import io.dts.server.model.GlobalLog;
import io.dts.server.model.GlobalTransactionState;
import io.dts.server.store.DtsLogDao;
import io.dts.server.store.DtsTransStatusDao;
import io.dts.server.store.impl.DtsServerRestorer;

/**
 * @author liushiming
 * @version ClientMessageHandler.java, v 0.0.1 2017年9月18日 下午5:38:29 liushiming
 */
public interface ClientMessageHandler {


  String processMessage(BeginMessage beginMessage, String clientIp);

  void processMessage(GlobalCommitMessage globalCommitMessage, String clientIp,
      DefaultDtsMessageHandler handler);

  String processMessage(GlobalRollbackMessage globalRollbackMessage, String clientIp,
      DefaultDtsMessageHandler handler);


  public static ClientMessageHandler createClientMessageProcessor(
      DtsTransStatusDao dtsTransStatusDao, DtsLogDao dtsLogDao) {

    return new ClientMessageHandler() {


      @Override
      public String processMessage(BeginMessage beginMessage, String clientIp) {
        GlobalLog globalLog = new GlobalLog();
        globalLog.setState(GlobalTransactionState.Begin.getValue());
        globalLog.setTimeout(beginMessage.getTimeout());
        globalLog.setClientAppName(clientIp);
        globalLog.setContainPhase2CommitBranch(false);
        dtsLogDao.insertGlobalLog(globalLog, 1);;
        long tranId = globalLog.getTxId();
        dtsTransStatusDao.insertGlobalLog(tranId, globalLog);
        String xid = TxcXID.generateXID(tranId);
        return xid;
      }

      @Override
      public void processMessage(GlobalCommitMessage globalCommitMessage, String clientIp,
          DefaultDtsMessageHandler handler) {
        Long tranId = globalCommitMessage.getTranId();
        GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
        if (globalLog == null) {
          // 事务已超时
          if (dtsTransStatusDao.queryTimeOut(tranId)) {
            dtsTransStatusDao.removeTimeOut(tranId);
            throw new DtsBizException(
                "transaction doesn't exist. It has been rollbacked because of timeout.");
          } // 事务已提交
          else if (DtsServerRestorer.restoredCommittingTransactions.contains(tranId)) {
            return;
          } // 在本地缓存未查到事务
          else {
            throw new DtsBizException("transaction doesn't exist.");
          }
        } else {
          switch (GlobalTransactionState.parse(globalLog.getState())) {
            case Committing:
              if (!globalLog.isContainPhase2CommitBranch()) {
                return;
              } else {
                throw new DtsBizException("transaction is committing.");
              }
            case Rollbacking:
              if (dtsTransStatusDao.queryTimeOut(tranId)) {
                dtsTransStatusDao.removeTimeOut(tranId);
                throw new DtsBizException("transaction is rollbacking because of timeout.");
              } else {
                throw new DtsBizException("transaction is rollbacking.");
              }
            case Begin:
            case CommitHeuristic:
              List<BranchLog> branchLogs =
                  dtsTransStatusDao.queryBranchLogByTransId(globalLog.getTxId());
              if (branchLogs.size() == 0) {
                dtsLogDao.deleteGlobalLog(globalLog.getTxId(), 1);
                dtsTransStatusDao.clearGlobalLog(tranId);
                return;
              }
              globalLog.setState(GlobalTransactionState.Committing.getValue());
              globalLog.setLeftBranches(branchLogs.size());
              dtsLogDao.updateGlobalLog(globalLog, 1);
              CommitGlobalTransaction commitGlobalTransaction =
                  new CommitGlobalTransaction(branchLogs);
              if (!globalLog.isContainPhase2CommitBranch()) {
                commitGlobalTransaction.doInsert(dtsTransStatusDao);
              } else {
                commitGlobalTransaction.doNotifyResourceManager(globalLog, handler);
              }
              return;
            default:
              throw new DtsBizException("Unknown state " + globalLog.getState());
          }

        }
      }

      @Override
      public String processMessage(GlobalRollbackMessage globalRollbackMessage, String clientIp,
          DefaultDtsMessageHandler handler) {
        long tranId = globalRollbackMessage.getTranId();
        GlobalLog globalLog = null;
        if (message.getRealSvrAddr() != null && this.clusterWorker != null) {
          globalLog = ClusterMessageHandlerImpl.getBkupGlobalLogMap().get(tranId);
        } else
          globalLog = activeTranMap.get(tranId);

        if (globalLog == null) {
          resultMessage.setResult(ResultCode.SYSTEMERROR.getValue());
          if (timeoutTranList.contains(tranId)) {
            resultMessage
                .setMsg("transaction doesn't exist. It has been rollbacked because of timeout.");
            timeoutTranList.remove(tranId);
          } else {
            resultMessage.setMsg("transaction doesn't exist.");
          }
        } else if (globalLog.getState() == GlobalTransactionState.Committing.getValue()) {
          resultMessage.setResult(ResultCode.SYSTEMERROR.getValue());
          resultMessage.setMsg("transaction is committing.");
        } else if (globalLog.getState() == GlobalTransactionState.Rollbacking.getValue()) {
          resultMessage.setResult(ResultCode.SYSTEMERROR.getValue());
          if (timeoutTranList.contains(tranId) && message.getRealSvrAddr() == null) {
            resultMessage.setMsg("transaction has been rollbacking because of timeout.");
            timeoutTranList.remove(tranId);
          } else {
            resultMessage.setMsg("transaction has been rollbacking.");
          }
        } else if (globalLog.getState() == GlobalTransactionState.Begin.getValue()) {
          List<BranchLog> branchLogs = null;
          branchLogs = this.getBranchLogs(tranId, true, true, message.getRealSvrAddr() != null);
          BranchLog.setLastBranchDelTrxKey(branchLogs);
          globalLog.setState(GlobalTransactionState.Rollbacking.getValue());
          stat.StatRollback(clientAppName, clientIp);
          try {
            if (message.getRealSvrAddr() == null)
              this.updateGlobalLog(globalLog);
          } catch (Exception e) {
            /**
             * 状态恢复为Begin
             */
            globalLog.setState(GlobalTransactionState.Begin.getValue());
            resultMessage.setMsg("update global status fail.");
            results[idx] = resultMessage;
            return;
          }

          try {
            if (message.getRealSvrAddr() == null)
              this.syncGlobalRollback(branchLogs, globalLog, resultMessage, clientIp, clientAppName,
                  tranId, msgId, false, results, idx);
            else
              this.globalRollbackForPrevNode(branchLogs, globalLog, resultMessage, clientIp,
                  clientAppName, tranId, msgId, false, message.getRealSvrAddr(), results, idx);
            return;
          } catch (Exception e) {
            logger.error(TxcErrCode.SyncGlobalRollback.errCode, e.getMessage(), e);
            resultMessage.setResult(ResultCode.SYSTEMERROR.getValue());
            resultMessage.setMsg("Catch Exception:" + e.getMessage());
          }
        } else {
          resultMessage.setResult(ResultCode.SYSTEMERROR.getValue());
          resultMessage.setMsg("Unknown state " + globalLog.getState());
        }
        results[idx] = resultMessage;
      }

    };
  }

  static class CommitGlobalTransaction {
    private final List<BranchLog> branchLogs;

    CommitGlobalTransaction(List<BranchLog> branchLogs) {
      this.branchLogs = branchLogs;
    }

    private void doInsert(DtsTransStatusDao dtsTransStatusDao) {
      for (BranchLog branchLog : branchLogs) {
        dtsTransStatusDao.insertCommitedBranchLog(branchLog.getBranchId(),
            BranchTransactionState.BEGIN.getValue());
      }
    }

    private void doNotifyResourceManager(GlobalLog globalLog, DefaultDtsMessageHandler handler) {
      Collections.sort(branchLogs, new Comparator<BranchLog>() {
        @Override
        public int compare(BranchLog o1, BranchLog o2) {
          return (int) (o1.getBranchId() - o2.getBranchId());
        }
      });
      handler.syncGlobalCommit(branchLogs, globalLog, globalLog.getTxId());
    }

  }
}
