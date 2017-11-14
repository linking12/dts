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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.dts.common.api.DtsServerMessageHandler;
import io.dts.common.api.DtsServerMessageSender;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.GlobalRollbackResultMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.server.handler.support.ClientMessageHandler;
import io.dts.server.handler.support.RmMessageHandler;
import io.dts.server.store.DtsLogDao;

/**
 * @author liushiming
 * @version DefaultDtsMessageHandler.java, v 0.0.1 2017年9月6日 下午5:33:51 liushiming
 */
@Component
@Scope("prototype")
public class DtsServerMessageHandlerImpl implements DtsServerMessageHandler {


  @Autowired
  private DtsLogDao dtsLogDao;

  @Autowired
  private DtsServerMessageSender serverMessageSender;


  private ClientMessageHandler clientHandler;

  private RmMessageHandler rmHandler;

  @PostConstruct
  public void init() {
    clientHandler =
        ClientMessageHandler.createClientMessageProcessor(dtsLogDao, serverMessageSender);
    rmHandler =
        RmMessageHandler.createResourceManagerMessageProcessor(dtsLogDao, serverMessageSender);
  }

  /**
   * 开始一个分布式事务
   */
  @Override
  public void handleMessage(String clientIp, BeginMessage message,
      BeginResultMessage resultMessage) {
    String xid = clientHandler.processMessage(message, clientIp);
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
    clientHandler.processMessage(message);
    return;
  }


  /**
   * 处理全局事务回滚
   */
  @Override
  public void handleMessage(String clientIp, GlobalRollbackMessage message,
      GlobalRollbackResultMessage resultMessage) {
    resultMessage.setTranId(message.getTranId());
    clientHandler.processMessage(message);
    return;
  }


  @Override
  public void handleMessage(String clientIp, RegisterMessage registerMessage,
      RegisterResultMessage resultMessage) {
    long tranId = registerMessage.getTranId();
    Long branchId = rmHandler.processMessage(registerMessage, clientIp);
    resultMessage.setBranchId(branchId);
    resultMessage.setTranId(tranId);
    return;
  }

}
