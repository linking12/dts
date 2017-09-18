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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.dts.common.api.DtsServerMessageHandler;
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
import io.dts.server.model.GlobalLog;
import io.dts.server.model.GlobalTransactionState;
import io.dts.server.resultcode.RollbackingResultCode;
import io.dts.server.store.DtsLogDao;
import io.dts.server.store.DtsTransStatusDao;

/**
 * @author liushiming
 * @version DefaultDtsMessageHandler.java, v 0.0.1 2017年9月6日 下午5:33:51 liushiming
 */
@Component
@Scope("prototype")
public class DefaultDtsMessageHandler implements DtsServerMessageHandler {

  private static final Logger logger = LoggerFactory.getLogger(DtsServerMessageHandler.class);

  @Autowired
  private DtsTransStatusDao dtsTransStatusDao;

  @Autowired
  private DtsLogDao dtsLogDao;

  /**
   * 开始一个分布式事务
   */
  @Override
  public void handleMessage(String clientIp, BeginMessage message,
      BeginResultMessage resultMessage) {
    ClientMessageHandler processor =
        ClientMessageHandler.createClientMessageProcessor(dtsTransStatusDao, dtsLogDao);
    String xid = processor.processMessage(message, clientIp);
    resultMessage.setXid(xid);
    return;
  }

  /**
   * 处理全局事务提交
   */
  @Override
  public void handleMessage(String clientIp, GlobalCommitMessage message,
      GlobalCommitResultMessage resultMessage) {
    resultMessage.setTranId(message.getTranId());
    ClientMessageHandler processor =
        ClientMessageHandler.createClientMessageProcessor(dtsTransStatusDao, dtsLogDao);
    processor.processMessage(message, clientIp, this);
  }


  @Override
  public void handleMessage(String clientIp, GlobalRollbackMessage message,
      GlobalRollbackResultMessage resultMessage) {
    resultMessage.setTranId(message.getTranId());
    ClientMessageHandler processor =
        ClientMessageHandler.createClientMessageProcessor(dtsTransStatusDao, dtsLogDao);
    processor.processMessage(message, clientIp, this);
  }

  // 往resourceManager发送消息
  // TODO
  protected void syncGlobalCommit(List<BranchLog> branchLogs, GlobalLog globalLog, long tranId) {

  }

  @Override
  public void handleMessage(String clientIp, RegisterMessage message,
      RegisterResultMessage resultMessage) {
    long tranId = message.getTranId();
    byte commitMode = message.getCommitMode();
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
    branchLog.setClientInfo(message.getKey());
    branchLog.setBusinessKey(message.getBusinessKey());
    branchLog.setClientIp(clientIp);
    branchLog.setState(BranchLogState.Begin.getValue());
    branchLog.setCommitMode(commitMode);
    dtsLogDao.insertBranchLog(branchLog, 1);
    dtsTransStatusDao.insertBranchLog(branchLog.getBranchId(), branchLog);
    globalLog.getBranchIds().add(branchLog.getBranchId());
    resultMessage.setBranchId(branchLog.getBranchId());
    resultMessage.setTranId((int) tranId);
    return;
  }

  @Override
  public void handleMessage(String clientIp, ReportStatusMessage message,
      ReportStatusResultMessage resultMessage) {
    resultMessage.setBranchId(message.getBranchId());
    BranchLog branchLog = dtsTransStatusDao.queryBranchLog(message.getBranchId());
    if (branchLog == null) {
      throw new DtsBizException("branch doesn't exist.");
    }
    int state = (message.isSuccess()) ? BranchLogState.Success.getValue()
        : BranchLogState.Failed.getValue();
    branchLog.setState(state);
    branchLog.setUdata(message.getUdata());
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
