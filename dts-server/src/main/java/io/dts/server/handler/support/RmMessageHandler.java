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

import io.dts.common.exception.DtsException;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.server.network.DtsServerContainer;
import io.dts.server.store.DtsLogDao;
import io.dts.server.store.DtsTransStatusDao;
import io.dts.server.struct.BranchLog;
import io.dts.server.struct.BranchLogState;
import io.dts.server.struct.GlobalLog;
import io.dts.server.struct.GlobalTransactionState;

/**
 * @author liushiming
 * @version RmMessageHandler.java, v 0.0.1 2017年9月19日 下午2:48:59 liushiming
 */
public interface RmMessageHandler {

  Long processMessage(RegisterMessage registerMessage, String clientIp);

  public static RmMessageHandler createResourceManagerMessageProcessor(
      DtsTransStatusDao dtsTransStatusDao, DtsLogDao dtsLogDao) {

    return new RmMessageHandler() {
      private final Logger logger = LoggerFactory.getLogger(RmMessageHandler.class);

      @Override
      public Long processMessage(RegisterMessage registerMessage, String clientIp) {
        long tranId = registerMessage.getTranId();
        GlobalLog globalLog = dtsTransStatusDao.queryGlobalLog(tranId);
        if (globalLog == null || globalLog.getState() != GlobalTransactionState.Begin.getValue()) {
          if (globalLog == null) {
            throw new DtsException("Transaction " + tranId + " doesn't exist");
          } else {
            throw new DtsException("Transaction " + tranId + " is in state:"
                + this.getStateString(GlobalTransactionState.class, globalLog.getState()));
          }
        }
        BranchLog branchLog = new BranchLog();
        branchLog.setTransId(tranId);
        branchLog.setWaitPeriods(0);
        branchLog.setClientAppName(clientIp);
        branchLog.setClientInfo(registerMessage.getKey());
        branchLog.setBusinessKey(registerMessage.getBusinessKey());
        branchLog.setClientIp(clientIp);
        branchLog.setState(BranchLogState.Begin.getValue());
        try {
          dtsLogDao.insertBranchLog(branchLog, DtsServerContainer.mid);
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
          throw new DtsException("insert branch log failed");
        }
        Long branchId = branchLog.getBranchId();
        dtsTransStatusDao.saveBranchLog(branchId, branchLog);
        globalLog.getBranchIds().add(branchId);
        return branchId;
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
