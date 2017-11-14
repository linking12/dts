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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.api.DtsServerMessageSender;
import io.dts.common.protocol.ResultCode;
import io.dts.common.protocol.header.BranchCommitResultMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.server.store.DtsLogDao;
import io.dts.server.struct.BranchLog;
import io.dts.server.struct.BranchLogState;

/**
 * @author liushiming
 * @version SyncRmMessagHandler.java, v 0.0.1 2017年9月19日 下午4:36:31 liushiming
 */
public interface SyncRmMessagHandler {

  void processMessage(String clientIp, BranchCommitResultMessage message);

  void processMessage(String clientIp, BranchRollbackResultMessage message);


  public static SyncRmMessagHandler createSyncGlobalResultProcess(final DtsLogDao dtsLogDao,
      final DtsServerMessageSender messageSender) {

    return new SyncRmMessagHandler() {

      private final Logger logger = LoggerFactory.getLogger(SyncRmMessagHandler.class);

      @Override
      public void processMessage(String clientIp, BranchCommitResultMessage message) {
        Long branchId = message.getBranchId();
        if (message.getResult() == ResultCode.OK.getValue()) {
          dtsLogDao.deleteBranchLog(branchId);
        } else if (message.getResult() == ResultCode.ERROR.getValue()) {
          synchronized (messageSender) {
            BranchLog branchLog = dtsLogDao.getBranchLog(branchId);
            branchLog.setState(BranchLogState.Failed.getValue());
            dtsLogDao.updateBranchLog(branchLog);
            dtsLogDao.insertBranchErrorLog(branchLog);
          }
          logger.error("Logic error occurs while commit branch:" + branchId
              + ". Please check server table:txc_branch_error_log.");
        }
      }

      @Override
      public void processMessage(String clientIp, BranchRollbackResultMessage message) {
        Long branchId = message.getBranchId();
        if (message.getResult() == ResultCode.OK.getValue()) {
          dtsLogDao.deleteBranchLog(branchId);
        } else if (message.getResult() == ResultCode.ERROR.getValue()) {
          synchronized (messageSender) {
            BranchLog branchLog = dtsLogDao.getBranchLog(branchId);
            branchLog.setState(BranchLogState.Failed.getValue());
            dtsLogDao.updateBranchLog(branchLog);
            dtsLogDao.insertBranchErrorLog(branchLog);
          }
          logger.error("Logic error occurs while rollback branch:" + message.getBranchId()
              + ". Please check server table:txc_branch_error_log.");
        }
      }
    };
  }
}
