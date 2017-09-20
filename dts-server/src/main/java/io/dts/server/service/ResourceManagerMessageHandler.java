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
package io.dts.server.service;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.common.CommitMode;
import io.dts.common.common.TxcXID;
import io.dts.common.protocol.header.BeginRetryBranchMessage;
import io.dts.common.protocol.header.BeginRetryBranchResultMessage;
import io.dts.common.protocol.header.QueryLockMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.ReportStatusMessage;
import io.dts.common.protocol.header.ReportUdataMessage;
import io.dts.server.exception.DtsBizException;
import io.dts.server.model.BranchLog;
import io.dts.server.model.BranchLogState;
import io.dts.server.model.GlobalLog;
import io.dts.server.model.GlobalTransactionState;
import io.dts.server.resultcode.CommitingResultCode;
import io.dts.server.resultcode.RollbackingResultCode;
import io.dts.server.store.DtsLogDao;
import io.dts.server.store.DtsTransStatusDao;

/**
 * @author liushiming
 * @version ResourceManagerMessageHandler.java, v 0.0.1 2017年9月19日 下午2:48:59 liushiming
 */
public interface ResourceManagerMessageHandler {

  Long processMessage(RegisterMessage registerMessage, String clientIp);

  void processMessage(ReportStatusMessage reportStatusMessage, String clientIp);

  void processMessage(QueryLockMessage queryLockMessage, String clientIp);

  void processMessage(ReportUdataMessage reportUdataMessage, String clientIp);

  void processMessage(BeginRetryBranchMessage beginRetryBranchMessage,
      BeginRetryBranchResultMessage resultMessage, String clientIp);

  public static ResourceManagerMessageHandler createResourceManagerMessageProcessor(
      DtsTransStatusDao dtsTransStatusDao, DtsLogDao dtsLogDao) {

    return new ResourceManagerMessageHandler() {
      private final Logger logger = LoggerFactory.getLogger(ResourceManagerMessageHandler.class);

      @Override
      public Long processMessage(RegisterMessage registerMessage, String clientIp) {
        long tranId = registerMessage.getTranId();
        byte commitMode = registerMessage.getCommitMode();
        GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
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
        branchLog.setClientInfo(registerMessage.getKey());
        branchLog.setBusinessKey(registerMessage.getBusinessKey());
        branchLog.setClientIp(clientIp);
        branchLog.setState(BranchLogState.Begin.getValue());
        branchLog.setCommitMode(commitMode);
        if (commitMode == CommitMode.COMMIT_IN_PHASE2.getValue())
          globalLog.setContainPhase2CommitBranch(true);
        try {
          dtsLogDao.insertBranchLog(branchLog, 1);
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
          throw new DtsBizException("insert branch log failed");
        }
        Long branchId = branchLog.getBranchId();
        dtsTransStatusDao.insertBranchLog(branchId, branchLog);
        globalLog.getBranchIds().add(branchId);
        return branchId;
      }

      @Override
      public void processMessage(ReportStatusMessage reportStatusMessage, String clientIp) {
        BranchLog branchLog = dtsTransStatusDao.queryBranchLog(reportStatusMessage.getBranchId());
        if (branchLog == null) {
          throw new DtsBizException("branch doesn't exist.");
        }
        int state = (reportStatusMessage.isSuccess()) ? BranchLogState.Success.getValue()
            : BranchLogState.Failed.getValue();
        branchLog.setState(state);
        branchLog.setUdata(reportStatusMessage.getUdata());
        dtsLogDao.updateBranchLog(branchLog, 1);
        /**
         * 如果事务因为超时而回滚，事务在rollbacking状态，需要把这个分支放入rollbackingMap
         */
        GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(branchLog.getTxId());
        if (globalLog == null) {
          throw new DtsBizException("global log doesn't exist.");
        }
        if (globalLog.getState() == GlobalTransactionState.Rollbacking.getValue()) {
          dtsTransStatusDao.insertRollbackBranchLog(branchLog.getBranchId(),
              RollbackingResultCode.TIMEOUT.getValue());
        }
      }

      @Override
      public void processMessage(QueryLockMessage queryLockMessage, String clientIp) {

        // TODO
      }

      @Override
      public void processMessage(ReportUdataMessage reportUdataMessage, String clientIp) {
        Long branchId = reportUdataMessage.getBranchId();
        BranchLog branchLog = dtsTransStatusDao.queryBranchLog(branchId);
        if (branchLog == null) {
          throw new DtsBizException("branch doesn't exist.");
        }
        if (reportUdataMessage.getUdata() != null) {
          branchLog.setUdata(reportUdataMessage.getUdata());
          try {
            dtsLogDao.updateBranchLog(branchLog, 1);
          } catch (Exception e) {
            throw new DtsBizException("update branchlog usedata failed");
          }
        }
      }

      @Override
      public void processMessage(BeginRetryBranchMessage beginRetryBranchMessage,
          BeginRetryBranchResultMessage resultMessage, String clientIp) {
        GlobalLog retryGlobalLog = dtsTransStatusDao.getRetryGlobalLog();
        if (retryGlobalLog == null) {
          retryGlobalLog = new GlobalLog();
          retryGlobalLog.setState(GlobalTransactionState.Committing.getValue());
          // if (this.clusterWorker != null) {
          retryGlobalLog.setTxId(dtsTransStatusDao.generateGlobalId());
          retryGlobalLog.setGmtCreated(Calendar.getInstance().getTime());
          retryGlobalLog.setGmtModified(retryGlobalLog.getGmtCreated());
          retryGlobalLog.setRecvTime(System.currentTimeMillis());
          // }
          try {
            dtsLogDao.insertGlobalLog(retryGlobalLog, 1);
          } catch (Exception e) {
            throw new DtsBizException("insert global retry log failed");
          }
          retryGlobalLog.setLeftBranches(1);
          retryGlobalLog.setTimeout(0);
          retryGlobalLog.setContainPhase2CommitBranch(false);
          dtsTransStatusDao.insertGlobalLog(retryGlobalLog.getTxId(), retryGlobalLog);
          dtsTransStatusDao.setRetryGlobalLog(retryGlobalLog);
        }
        long tranId = retryGlobalLog.getTxId();
        String xid = TxcXID.generateXID(tranId);
        resultMessage.setXid(xid);
        BranchLog branchLog = new BranchLog();
        branchLog.setTxId(tranId);
        branchLog.setWaitPeriods(0);
        branchLog.setClientInfo(beginRetryBranchMessage.getDbName());
        branchLog.setClientIp(clientIp);
        branchLog.setState(BranchLogState.Success.getValue());
        branchLog.setCommitMode(CommitMode.COMMIT_RETRY_MODE.getValue());
        branchLog.setUdata(Long.toString(beginRetryBranchMessage.getEffectiveTime()));
        branchLog.setRetrySql(beginRetryBranchMessage.getSql());
        // if (this.clusterWorker != null) {
        branchLog.setBranchId(dtsTransStatusDao.generateBranchId());
        branchLog.setGmtCreated(Calendar.getInstance().getTime());
        branchLog.setGmtModified(branchLog.getGmtCreated());
        branchLog.setRecvTime(System.currentTimeMillis());
        // }
        try {
          dtsLogDao.insertBranchLog(branchLog, 1);
        } catch (Exception e) {
          throw new DtsBizException("insert branch retry log failed");
        }
        dtsTransStatusDao.insertBranchLog(branchLog.getBranchId(), branchLog);
        retryGlobalLog.getBranchIds().add(branchLog.getBranchId());
        retryGlobalLog.increaseLeftBranches();
        resultMessage.setBranchId(branchLog.getBranchId());
        dtsTransStatusDao.insertCommitedBranchLog(branchLog.getBranchId(),
            CommitingResultCode.BEGIN.getValue());
      }


      private String getStateString(Class<?> cl, int value) {
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

    };
  }



}
