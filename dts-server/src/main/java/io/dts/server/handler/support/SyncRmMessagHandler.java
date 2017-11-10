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

import io.dts.common.protocol.ResultCode;
import io.dts.common.protocol.header.BranchCommitResultMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.server.handler.CommitingResultCode;
import io.dts.server.handler.RollbackingResultCode;
import io.dts.server.network.DtsServerContainer;
import io.dts.server.store.DtsLogDao;
import io.dts.server.store.DtsTransStatusDao;
import io.dts.server.struct.BranchLog;
import io.dts.server.struct.GlobalLog;

/**
 * @author liushiming
 * @version SyncRmMessagHandler.java, v 0.0.1 2017年9月19日 下午4:36:31 liushiming
 */
public interface SyncRmMessagHandler {

  void processMessage(String clientIp, BranchCommitResultMessage message);

  void processMessage(String clientIp, BranchRollbackResultMessage message);


  public static SyncRmMessagHandler createSyncGlobalResultProcess(
      DtsTransStatusDao dtsTransStatusDao, DtsLogDao dtsLogDao) {

    return new SyncRmMessagHandler() {

      private final Logger logger = LoggerFactory.getLogger(SyncRmMessagHandler.class);

      @Override
      public void processMessage(String clientIp, BranchCommitResultMessage message) {
        // 如果该resourceManager下面的branch提交成功
        Long tranId = message.getTranId();
        Long branchId = message.getBranchId();
        if (message.getResult() == ResultCode.OK.getValue()) {
          if (dtsTransStatusDao.clearCommitedResult(branchId)) {
            return;
          }
          BranchLog branchLog = dtsTransStatusDao.clearBranchLog(branchId);
          if (branchLog != null) {
            dtsLogDao.deleteBranchLog(branchLog, DtsServerContainer.mid);
          }
          GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
          synchronized (globalLog) {
            globalLog.getBranchIds().remove(branchId);
            int leftBranches = globalLog.getLeftBranches();
            if (leftBranches == 0) {
              dtsTransStatusDao.clearGlobalLog(tranId);
              dtsLogDao.deleteGlobalLog(tranId, DtsServerContainer.mid);
            }
          }

        } else if (message.getResult() == ResultCode.SYSTEMERROR.getValue()) {
          dtsTransStatusDao.insertCommitedResult(branchId, CommitingResultCode.FAILED.getValue());
        } // 如果出现了逻辑错误，需要发出告警
        else if (message.getResult() == ResultCode.LOGICERROR.getValue()) {
          if (!dtsTransStatusDao.clearCommitedResult(branchId)) {
            return;
          }
          BranchLog branchLog = dtsTransStatusDao.clearBranchLog(branchId);
          if (branchLog != null) {
            dtsLogDao.insertBranchErrorLog(branchLog, DtsServerContainer.mid);
            dtsLogDao.deleteBranchLog(branchLog, 1);
            logger.error("Logic error occurs while commit branch:" + branchId
                + ". Please check server table:txc_branch_error_log.");
          }
          GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
          synchronized (globalLog) {
            globalLog.getBranchIds().remove(branchId);
            int leftBranches = globalLog.getLeftBranches();
            if (leftBranches == 0) {
              dtsTransStatusDao.clearGlobalLog(tranId);
              dtsLogDao.deleteGlobalLog(tranId, 1);

            }
          }
        } else {
          dtsTransStatusDao.insertCommitedResult(branchId, CommitingResultCode.FAILED.getValue());

        }
      }

      @Override
      public void processMessage(String clientIp, BranchRollbackResultMessage message) {
        Long tranId = message.getTranId();
        Long branchId = message.getBranchId();
        if (message.getResult() == ResultCode.OK.getValue()) {
          if (dtsTransStatusDao.clearRollbackResult(branchId)) {
            return;
          }
          BranchLog branchLog = dtsTransStatusDao.clearBranchLog(branchId);
          if (branchLog != null) {
            dtsLogDao.deleteBranchLog(branchLog, DtsServerContainer.mid);
          }
          GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
          synchronized (globalLog) {
            globalLog.getBranchIds().remove(branchId);
            int leftBranches = globalLog.getLeftBranches();
            if (leftBranches == 0) {
              dtsTransStatusDao.clearGlobalLog(tranId);
              dtsLogDao.deleteGlobalLog(tranId, DtsServerContainer.mid);
            }
          }
        } else if (message.getResult() == ResultCode.SYSTEMERROR.getValue()) {
          dtsTransStatusDao.insertRollbackResult(branchId, RollbackingResultCode.FAILED.getValue());
        } else if (message.getResult() == ResultCode.LOGICERROR.getValue()) {
          if (!dtsTransStatusDao.clearRollbackResult(branchId)) {
            return;
          }
          BranchLog branchLog = dtsTransStatusDao.clearBranchLog(branchId);
          if (branchLog != null) {
            dtsLogDao.insertBranchErrorLog(branchLog, DtsServerContainer.mid);
            dtsLogDao.deleteBranchLog(branchLog, DtsServerContainer.mid);
            logger.error("Logic error occurs while rollback branch:" + message.getBranchId()
                + ". Please check server table:txc_branch_error_log.");
          }
          GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
          synchronized (globalLog) {
            globalLog.getBranchIds().remove(branchId);
            int leftBranches = globalLog.getLeftBranches();
            if (leftBranches == 0) {
              dtsTransStatusDao.clearGlobalLog(tranId);
              dtsLogDao.deleteGlobalLog(tranId, 1);

            }
          }
        } else {
          dtsTransStatusDao.insertRollbackResult(branchId, RollbackingResultCode.FAILED.getValue());
        }
      }

    };
  }
}
