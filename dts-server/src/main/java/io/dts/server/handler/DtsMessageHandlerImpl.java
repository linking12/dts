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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.dts.common.common.TxcXID;
import io.dts.common.protocol.body.BranchCommitResultMessage;
import io.dts.common.protocol.body.DtsMultipleRequestMessage;
import io.dts.common.protocol.body.DtsMultipleResonseMessage;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.BeginRetryBranchMessage;
import io.dts.common.protocol.header.BeginRetryBranchResultMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.GlobalRollbackResultMessage;
import io.dts.common.protocol.header.QueryLockMessage;
import io.dts.common.protocol.header.QueryLockResultMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.common.protocol.header.ReportStatusMessage;
import io.dts.common.protocol.header.ReportStatusResultMessage;
import io.dts.common.protocol.header.ReportUdataMessage;
import io.dts.common.protocol.header.ReportUdataResultMessage;
import io.dts.server.exception.DtsBizException;
import io.dts.server.model.BranchLog;
import io.dts.server.model.BranchLogState;
import io.dts.server.model.CommitingResultCode;
import io.dts.server.model.GlobalLog;
import io.dts.server.model.GlobalTransactionState;
import io.dts.server.store.TxcServerRestorer;
import io.dts.server.util.RollbackingResultCode;

/**
 * @author liushiming
 * @version DtsServerMessageListener.java, v 0.0.1 2017年9月6日 下午5:33:51 liushiming
 */
@Component
public class DtsMessageHandlerImpl implements DtsMessageHandler {

  private static final Logger logger = LoggerFactory.getLogger(DtsMessageHandler.class);
  /**
   * 当前活动的所有事务
   */
  private static Map<Long, GlobalLog> activeTranMap = new ConcurrentHashMap<Long, GlobalLog>();

  /**
   * 当前活动的所有事务分支
   */
  private static Map<Long, BranchLog> activeTranBranchMap =
      new ConcurrentHashMap<Long, BranchLog>();

  /**
   * 保存已经发送BranchCommitMessage消息，但是还没收到响应或者失败的分支
   */
  private static Map<Long, Integer> committingMap = new ConcurrentHashMap<Long, Integer>();

  /**
   * 保存已经发送BranchRollbackMessage消息，但是还没收到响应或者失败的分支
   */
  private static Map<Long, Integer> rollbackingMap = new ConcurrentHashMap<Long, Integer>();

  /**
   * 超时的事务列表
   */
  private static List<Long> timeoutTranList = Collections.synchronizedList(new ArrayList<Long>());

  @Override
  public void handleMessage(String clientIp, BeginMessage message,
      BeginResultMessage resultMessage) {
    GlobalLog globalLog = new GlobalLog();
    globalLog.setState(GlobalTransactionState.Begin.getValue());
    globalLog.setTimeout(message.getTimeout());
    globalLog.setClientAppName(clientIp);
    globalLog.setContainPhase2CommitBranch(false);
    // TODO
    // this.insertGlobalLog(globalLog);
    //
    long tranId = globalLog.getTxId();
    activeTranMap.put(tranId, globalLog);
    String xid = TxcXID.generateXID(tranId);
    resultMessage.setXid(xid);
    return;
  }

  @Override
  public void handleMessage(String clientIp, GlobalCommitMessage message,
      GlobalCommitResultMessage resultMessage) {
    resultMessage.setTranId(message.getTranId());
    GlobalLog globalLog = activeTranMap.get(message.getTranId());
    if (globalLog == null) {
      // 事务已超时
      if (timeoutTranList.contains(message.getTranId())) {
        timeoutTranList.remove(message.getTranId());
        throw new DtsBizException(
            "transaction doesn't exist. It has been rollbacked because of timeout.");
      } // 事务已提交
      else if (TxcServerRestorer.restoredCommittingTransactions.contains(message.getTranId())) {
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
          if (timeoutTranList.contains(message.getTranId())) {
            timeoutTranList.remove(message.getTranId());
            throw new DtsBizException("transaction is rollbacking because of timeout.");
          } else {
            throw new DtsBizException("transaction is rollbacking.");
          }
        case Begin:
        case CommitHeuristic:
          class CommitGlobalTransaction {
            private final GlobalLog globalLog;
            private final List<BranchLog> branchLogs;

            CommitGlobalTransaction(final GlobalLog globalLog) {
              this.globalLog = globalLog;
              try {
                branchLogs = getBranchLogs(globalLog.getTxId());
              } catch (Exception e) {
                throw new DtsBizException("get branch logs fail. " + e.getMessage());
              }
            }

            private List<BranchLog> queryBranchLogs() {
              return branchLogs;
            }

            private void commitBranchLog() {
              try {
                if (!globalLog.isContainPhase2CommitBranch()) {
                  for (BranchLog branchLog : branchLogs) {
                    committingMap.put(branchLog.getBranchId(),
                        CommitingResultCode.BEGIN.getValue());
                  }
                } else {
                  Collections.sort(branchLogs, new Comparator<BranchLog>() {
                    @Override
                    public int compare(BranchLog o1, BranchLog o2) {
                      return (int) (o1.getBranchId() - o2.getBranchId());
                    }
                  });
                  syncGlobalCommit(branchLogs, globalLog, globalLog.getTxId());
                }
              } catch (Exception e) {
                logger.error("errorCode", e.getMessage(), e);
              }
            }
          }
          CommitGlobalTransaction commitGlobalTransaction = new CommitGlobalTransaction(globalLog);
          List<BranchLog> branchLogs = commitGlobalTransaction.queryBranchLogs();
          if (branchLogs.size() == 0) {
            // TODO
            // this.deleteGlobalLog(globalLog);
            activeTranMap.remove(message.getTranId());
            return;
          }
          globalLog.setState(GlobalTransactionState.Committing.getValue());
          globalLog.setLeftBranches(branchLogs.size());
          // TODO
          // this.updateGlobalLog(globalLog);
          commitGlobalTransaction.commitBranchLog();
          break;

        default:
          break;
      }

    }

  }


  public void syncGlobalCommit(List<BranchLog> branchLogs, GlobalLog globalLog, long tranId) {

  }

  public List<BranchLog> getBranchLogs(long tranId) {
    return getBranchLogs(tranId, false, false, false);
  }


  public List<BranchLog> getBranchLogs(long tranId, boolean sort, boolean reverse,
      boolean fromBkup) {
    List<BranchLog> branchLogs = new ArrayList<BranchLog>();
    Map<Long, BranchLog> branchMap = activeTranBranchMap;
    GlobalLog globalLog;
    globalLog = activeTranMap.get(tranId);
    if (globalLog == null)
      return branchLogs;
    for (long branchId : globalLog.getBranchIds()) {
      BranchLog branchLog;
      if ((branchLog = branchMap.get(branchId)) != null) {
        branchLogs.add(branchLog);
      }
    }
    if (sort) {
      if (reverse) {
        Collections.sort(branchLogs, new Comparator<BranchLog>() {
          @Override
          public int compare(BranchLog o1, BranchLog o2) {
            return (int) (o2.getBranchId() - o1.getBranchId());
          }
        });
      } else {
        Collections.sort(branchLogs, new Comparator<BranchLog>() {
          @Override
          public int compare(BranchLog o1, BranchLog o2) {
            return (int) (o1.getBranchId() - o2.getBranchId());
          }
        });
      }
    }
    return branchLogs;
  }


  @Override
  public void handleMessage(String clientIp, GlobalRollbackMessage message,
      GlobalRollbackResultMessage resultMessage) {

  }

  @Override
  public void handleMessage(String clientIp, RegisterMessage message,
      RegisterResultMessage resultMessage) {
    long tranId = message.getTranId();
    byte commitMode = message.getCommitMode();
    GlobalLog globalLog = activeTranMap.get(tranId);
    if (globalLog == null || globalLog.getState() != GlobalTransactionState.Begin.getValue()) {
      if (globalLog == null) {
        throw new DtsBizException("Transaction " + tranId + " doesn't exist");
      } else {
        throw new DtsBizException("Transaction " + tranId + " is in state:"
            + this.getStateString(GlobalTransactionState.class, globalLog.getState()));
      }
    }
    BranchLog branchLog = new BranchLog();
    branchLog.setTxId(tranId);
    branchLog.setWaitPeriods(0);
    branchLog.setClientAppName(clientIp);
    branchLog.setClientInfo(message.getKey());
    branchLog.setBusinessKey(message.getBusinessKey());
    branchLog.setClientIp(clientIp);
    branchLog.setState(BranchLogState.Begin.getValue());
    branchLog.setCommitMode(commitMode);
    // TODO
    // this.insertBranchLog(branchLog);
    activeTranBranchMap.put(branchLog.getBranchId(), branchLog);
    globalLog.getBranchIds().add(branchLog.getBranchId());
    resultMessage.setBranchId(branchLog.getBranchId());
    resultMessage.setTranId((int) tranId);
    return;
  }

  @Override
  public void handleMessage(String clientIp, ReportStatusMessage message,
      ReportStatusResultMessage resultMessage) {
    resultMessage.setBranchId(message.getBranchId());
    BranchLog branchLog = activeTranBranchMap.get(message.getBranchId());
    if (branchLog == null) {
      throw new DtsBizException("branch doesn't exist.");
    }
    int state = (message.isSuccess()) ? BranchLogState.Success.getValue()
        : BranchLogState.Failed.getValue();
    branchLog.setState(state);
    boolean optimized = true;
    if (message.getUdata() != null) {
      branchLog.setUdata(message.getUdata());
      optimized = false;
    }
    // TODO
    // this.updataBranchLog(branchLog, optimized);
    GlobalLog globalLog = activeTranMap.get(branchLog.getTxId());
    if (globalLog == null) {
      throw new DtsBizException("global log doesn't exist.");
    }
    if (globalLog.getState() == GlobalTransactionState.Rollbacking.getValue()) {
      rollbackingMap.put(branchLog.getBranchId(), RollbackingResultCode.TIMEOUT.getValue());
    }
  }

  @Override
  public void handleMessage(String clientIp, BeginRetryBranchMessage message,
      BeginRetryBranchResultMessage resultMessage) {

  }

  @Override
  public void handleMessage(String clientIp, ReportUdataMessage message,
      ReportUdataResultMessage resultMessage) {

  }

  @Override
  public void handleMessage(String clientIp, DtsMultipleRequestMessage message,
      DtsMultipleResonseMessage resultMessage) {

  }

  @Override
  public void handleMessage(String clientIp, QueryLockMessage message,
      QueryLockResultMessage resultMessage) {

  }

  @Override
  public void handleMessage(String clientIp, BranchCommitResultMessage message) {

  }

  @Override
  public void handleMessage(String clientIp, BranchRollbackResultMessage message) {

  }

  public String getStateString(Class<?> cl, int value) {
    if (cl.equals(GlobalTransactionState.class)) {
      switch (value) {
        case 1:
          return "begin";
        case 2:
          return "committed";
        case 3:
          return "rollbacked";
        case 4:
          return "committing";
        case 5:
          return "rollbacking";
        default:
          return "unknown";
      }
    }
    return "unknown";
  }



}
