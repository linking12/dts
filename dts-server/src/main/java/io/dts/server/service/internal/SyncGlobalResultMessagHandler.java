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
import io.dts.server.resultcode.CommitingResultCode;
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
        int size = message.getTranIds().size();
        if (message.getResult() == ResultCode.OK.getValue()) {
          for (int i = 0; i < size; i++) {
            long branchId = message.getBranchIds().get(i);
            long tranId = message.getTranIds().get(i);
            if (committingMap.remove(branchId) == null)
              continue;
            BranchLog branchLog = activeTranBranchMap.get(branchId);
            if (branchLog == null)
              continue;
            try {
              this.deleteBranchLog(branchLog);
              activeTranBranchMap.remove(branchLog.getBranchId());
              GlobalLog globalLog = activeTranMap.get(branchLog.getTxId());
              if (globalLog != null)
                globalLog.getBranchIds().remove(branchLog.getBranchId());
            } catch (Exception e1) {
            }

            GlobalLog globalLog = activeTranMap.get(tranId);
            if (globalLog == null)
              continue;

            synchronized (globalLog) {
              int leftBranches = globalLog.getLeftBranches();
              if (logger.isDebugEnabled())
                logger.debug("remove branch:" + branchId + ", left:" + (leftBranches - 1));
              if (leftBranches <= 1) {
                // It is the last branches, do double check
                List<BranchLog> branchLogs = this.getBranchLogs(tranId);
                if (branchLogs.size() > 0) {
                  if (logger.isDebugEnabled()) {
                    logger.debug("There is left branch on transaction " + tranId);
                    logger.debug("left branches:" + branchLogs);
                  }
                } else {
                  try {
                    this.deleteGlobalLog(globalLog);
                    activeTranMap.remove(tranId);
                  } catch (Exception e1) {
                  }
                }
              } else {
                globalLog.decreaseLeftBranches();
              }
            }
          }
        } else if (message.getResult() == ResultCode.LOGICERROR.getValue()) {
          for (int i = 0; i < size; i++) {
            long branchId = message.getBranchIds().get(i);
            long tranId = message.getTranIds().get(i);
            if (committingMap.remove(branchId) == null)
              return;
            try {
              BranchLog branchLog = activeTranBranchMap.get(branchId);
              if (branchLog == null)
                return;

              dao.insertBranchErrorLog(branchLog, RpcServer.mid);
              try {
                this.deleteBranchLog(branchLog);
                activeTranBranchMap.remove(branchId);
                GlobalLog globalLog = activeTranMap.get(branchLog.getTxId());
                if (globalLog != null)
                  globalLog.getBranchIds().remove(branchLog.getBranchId());
              } catch (Exception e1) {
              }

              logger.error(TxcErrCode.RollBackLogic.errCode,
                  "Logic error occurs while commit branch:" + branchId
                      + ". Please check server table:txc_branch_error_log.");
              GlobalLog globalLog = TxcServerMessageListener.getActiveTranMap().get(tranId);
              if (globalLog == null) {
                logger.warn("In branchCommitResultHandle, global log of " + tranId
                    + " doesn't exist while branch " + branchId + " exists.");
                return;
              }

              synchronized (globalLog) {
                int leftBranches = globalLog.getLeftBranches();
                logger.info("remove branch:" + branchId + ", left:" + (leftBranches - 1));

                if (leftBranches > 1) {
                  globalLog.decreaseLeftBranches();
                } else {
                  try {
                    this.deleteGlobalLog(globalLog);
                    activeTranMap.remove(tranId);
                  } catch (Exception e1) {
                  }
                  this.cleanBranches(globalLog);
                  logger.info(
                      "Transaction:" + tranId + " is committed with error branch:" + branchId);
                }
              }
            } catch (Exception e) {
              logger.error("errorCode", e.getMessage(), e);
            }
          }
        } else {
          for (long branchId : message.getBranchIds())
            committingMap.put(branchId, CommitingResultCode.FAILED.getValue());
        }
      }

      @Override
      public void processMessage(String clientIp, BranchRollbackResultMessage message) {
        // TODO Auto-generated method stub

      }

    };
  }
}
