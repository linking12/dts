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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.dts.common.api.DtsServerMessageHandler;
import io.dts.common.protocol.RequestHeaderMessage;
import io.dts.common.protocol.ResponseMessage;
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
import io.dts.server.model.BranchLog;
import io.dts.server.model.GlobalLog;
import io.dts.server.store.DtsLogDao;
import io.dts.server.store.DtsTransStatusDao;

/**
 * @author liushiming
 * @version DefaultDtsMessageHandler.java, v 0.0.1 2017年9月6日 下午5:33:51 liushiming
 */
@Component
@Scope("prototype")
public class DefaultDtsMessageHandler implements DtsServerMessageHandler {

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


  /**
   * 处理全局事务回滚
   */
  @Override
  public void handleMessage(String clientIp, GlobalRollbackMessage message,
      GlobalRollbackResultMessage resultMessage) {
    resultMessage.setTranId(message.getTranId());
    ClientMessageHandler processor =
        ClientMessageHandler.createClientMessageProcessor(dtsTransStatusDao, dtsLogDao);
    processor.processMessage(message, clientIp, this);
  }

  // 往resourceManager发送提交信息消息
  // TODO
  protected void syncGlobalCommit(List<BranchLog> branchLogs, GlobalLog globalLog, long tranId) {

  }

  // 往resourceManager发送全局回滚消息
  // TODO
  protected void syncGlobalRollback(List<BranchLog> branchLogs, GlobalLog globalLog, long tranId) {

  }

  @Override
  public void handleMessage(String clientIp, RegisterMessage registerMessage,
      RegisterResultMessage resultMessage) {
    long tranId = registerMessage.getTranId();
    ResourceManagerMessageHandler proccessor = ResourceManagerMessageHandler
        .createResourceManagerMessageProcessor(dtsTransStatusDao, dtsLogDao);
    Long branchId = proccessor.processMessage(registerMessage, clientIp);
    resultMessage.setBranchId(branchId);
    resultMessage.setTranId(tranId);
    return;
  }

  @Override
  public void handleMessage(String clientIp, ReportStatusMessage reportStatusMessage,
      ReportStatusResultMessage resultMessage) {
    resultMessage.setBranchId(reportStatusMessage.getBranchId());
    ResourceManagerMessageHandler proccessor = ResourceManagerMessageHandler
        .createResourceManagerMessageProcessor(dtsTransStatusDao, dtsLogDao);
    proccessor.processMessage(reportStatusMessage, clientIp);
    return;
  }

  @Override
  public void handleMessage(String clientIp, QueryLockMessage queryLockMessage,
      QueryLockResultMessage resultMessage) {
    resultMessage.setTranId(queryLockMessage.getTranId());
    resultMessage.setTranId(queryLockMessage.getTranId());
    ResourceManagerMessageHandler proccessor = ResourceManagerMessageHandler
        .createResourceManagerMessageProcessor(dtsTransStatusDao, dtsLogDao);
    proccessor.processMessage(queryLockMessage, clientIp);
    return;
  }


  @Override
  public void handleMessage(String clientIp, BeginRetryBranchMessage beginRetryBranchMessage,
      BeginRetryBranchResultMessage beginRetryBranchResultMessage) {
    ResourceManagerMessageHandler proccessor = ResourceManagerMessageHandler
        .createResourceManagerMessageProcessor(dtsTransStatusDao, dtsLogDao);
    proccessor.processMessage(beginRetryBranchMessage, beginRetryBranchResultMessage, clientIp);
  }

  @Override
  public void handleMessage(String clientIp, ReportUdataMessage reportUdataMessage,
      ReportUdataResultMessage resultMessage) {
    ResourceManagerMessageHandler proccessor = ResourceManagerMessageHandler
        .createResourceManagerMessageProcessor(dtsTransStatusDao, dtsLogDao);
    proccessor.processMessage(reportUdataMessage, clientIp);
  }

  @Override
  public void handleMessage(String clientIp, DtsMultipleRequestMessage message,
      DtsMultipleResonseMessage resultMessage) {
    List<RequestHeaderMessage> headerMessages = message.getMsgs();
    for (int i = 0; i < headerMessages.size(); i++) {
      final RequestHeaderMessage msg = headerMessages.get(i);
      ResponseMessage responseMessage = null;
      if (msg instanceof RegisterMessage) {
        responseMessage = new RegisterResultMessage();
        this.handleMessage(clientIp, (RegisterMessage) msg,
            (RegisterResultMessage) responseMessage);
      } else if (msg instanceof ReportStatusMessage) {
        responseMessage = new ReportStatusResultMessage();
        this.handleMessage(clientIp, (ReportStatusMessage) msg,
            (ReportStatusResultMessage) responseMessage);
      } else if (msg instanceof ReportUdataMessage) {
        responseMessage = new ReportUdataResultMessage();
        this.handleMessage(clientIp, (ReportUdataMessage) msg,
            (ReportUdataResultMessage) responseMessage);
      }
      if (responseMessage != null) {
        resultMessage.getMsgs()[i] = responseMessage;
      }
    }

  }

  @Override
  public void handleMessage(String clientIp, BranchCommitResultMessage message) {

  }

  @Override
  public void handleMessage(String clientIp, BranchRollbackResultMessage message) {

  }



}
