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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.common.CommitMode;
import io.dts.common.protocol.header.QueryLockMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.ReportStatusMessage;
import io.dts.server.exception.DtsBizException;
import io.dts.server.model.BranchLog;
import io.dts.server.model.BranchLogState;
import io.dts.server.model.GlobalLog;
import io.dts.server.model.GlobalTransactionState;
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

      }

      @Override
      public void processMessage(QueryLockMessage queryLockMessage, String clientIp) {

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
