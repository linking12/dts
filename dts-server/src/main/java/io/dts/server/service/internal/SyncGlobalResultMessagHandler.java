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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.protocol.ResultCode;
import io.dts.common.protocol.body.BranchCommitResultMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.server.model.BranchLog;
import io.dts.server.model.GlobalLog;
import io.dts.server.service.CommitingResultCode;
import io.dts.server.store.DtsLogDao;
import io.dts.server.store.DtsTransStatusDao;

/**
 * @author liushiming
 * @version SyncGlobalResultMessagHandler.java, v 0.0.1 2017年9月19日 下午4:36:31 liushiming
 */
public interface SyncGlobalResultMessagHandler {

  void processMessage(String clientIp, BranchCommitResultMessage message);

  void processMessage(String clientIp, BranchRollbackResultMessage message);


  public static SyncGlobalResultMessagHandler createSyncGlobalResultProcess(
      DtsTransStatusDao dtsTransStatusDao, DtsLogDao dtsLogDao) {

    return new SyncGlobalResultMessagHandler() {

      private final Logger logger = LoggerFactory.getLogger(SyncGlobalResultMessagHandler.class);

      @Override
      public void processMessage(String clientIp, BranchCommitResultMessage message) {
        // 如果该resourceManager下面的branch提交成功
        Long tranId = message.getTranId();
        List<Long> branchIds = message.getBranchIds();
        if (message.getResult() == ResultCode.OK.getValue()) {
          branchIds.forEach(branchId -> {
            if (dtsTransStatusDao.clearCommitedBranchLog(branchId)) {
              return;
            }
            BranchLog branchLog = dtsTransStatusDao.clearBranchLog(branchId);
            if (branchLog != null) {
              dtsLogDao.deleteBranchLog(branchLog, 1);
            }
            GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
            synchronized (globalLog) {
              globalLog.getBranchIds().removeAll(branchIds);
              int leftBranches = globalLog.getLeftBranches();
              if (leftBranches == 0) {
                dtsTransStatusDao.clearGlobalLog(tranId);
                dtsLogDao.deleteGlobalLog(tranId, 1);
              }
            }
          });
          // 如果出现了逻辑错误，需要发出告警
        } else if (message.getResult() == ResultCode.LOGICERROR.getValue()) {
          branchIds.forEach(branchId -> {
            if (dtsTransStatusDao.clearCommitedBranchLog(branchId)) {
              return;
            }
            BranchLog branchLog = dtsTransStatusDao.clearBranchLog(branchId);
            if (branchLog != null) {
              dtsLogDao.insertBranchErrorLog(branchLog, 1);
              dtsLogDao.deleteBranchLog(branchLog, 1);
              logger.error("Logic error occurs while commit branch:" + branchId
                  + ". Please check server table:txc_branch_error_log.");
            }
            GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
            synchronized (globalLog) {
              globalLog.getBranchIds().removeAll(branchIds);
              int leftBranches = globalLog.getLeftBranches();
              if (leftBranches == 0) {
                dtsTransStatusDao.clearGlobalLog(tranId);
                dtsLogDao.deleteGlobalLog(tranId, 1);

              }
            }
          });
          // 提交失败，可以重复提交
        } else {
          branchIds.forEach(branchId -> {
            dtsTransStatusDao.insertCommitedBranchLog(branchId,
                CommitingResultCode.FAILED.getValue());

          });

        }
      }

      @Override
      public void processMessage(String clientIp, BranchRollbackResultMessage message) {
        // TODO Auto-generated method stub

      }

    };
  }
}
