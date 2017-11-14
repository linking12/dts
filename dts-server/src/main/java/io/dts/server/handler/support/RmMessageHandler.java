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

import io.dts.common.api.DtsServerMessageSender;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.server.store.DtsLogDao;
import io.dts.server.struct.BranchLog;
import io.dts.server.struct.BranchLogState;
import io.dts.server.struct.GlobalLog;
import io.dts.server.struct.GlobalLogState;

/**
 * @author liushiming
 * @version RmMessageHandler.java, v 0.0.1 2017年9月19日 下午2:48:59 liushiming
 */
public interface RmMessageHandler {

  Long processMessage(RegisterMessage registerMessage, String clientIp);

  public static RmMessageHandler createResourceManagerMessageProcessor(DtsLogDao dtsLogDao,
      DtsServerMessageSender messageSender) {

    return new RmMessageHandler() {

      @Override
      public Long processMessage(RegisterMessage registerMessage, String clientIp) {
        long tranId = registerMessage.getTranId();
        GlobalLog globalLog = dtsLogDao.getGlobalLog(tranId);
        if (globalLog == null || globalLog.getState() != GlobalLogState.Begin.getValue()) {
          if (globalLog == null) {
            throw new DtsException("Transaction " + tranId + " doesn't exist");
          } else {
            throw new DtsException(
                "Transaction " + tranId + " is in state:" + globalLog.getState());
          }
        }
        BranchLog branchLog = new BranchLog();
        branchLog.setTransId(tranId);
        branchLog.setClientInfo(registerMessage.getDbName());
        branchLog.setClientIp(clientIp);
        branchLog.setState(BranchLogState.Begin.getValue());
        dtsLogDao.insertBranchLog(branchLog);
        Long branchId = branchLog.getBranchId();
        globalLog.getBranchIds().add(branchId);
        return branchId;
      }

    };
  }



}
