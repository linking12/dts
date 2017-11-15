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
package io.dts.server.network.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.dts.common.api.DtsServerMessageHandler;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.RequestMessage;
import io.dts.common.protocol.ResponseCode;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.GlobalRollbackResultMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.common.util.NetUtil;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author liushiming
 * @version MessageProcessorComponent.java, v 0.0.1 2017年9月13日 下午2:00:41 liushiming
 */
@Component
@Qualifier("dtsMessageProcessor")
@Scope("prototype")
public class DtsMessageProcessor implements NettyRequestProcessor {
  private static final Logger logger = LoggerFactory.getLogger(DtsMessageProcessor.class);

  @Lookup
  protected DtsServerMessageHandler createMessageHandler() {
    return null;
  }

  @Override
  public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
      throws Exception {
    final String clientIp = NetUtil.toStringAddress(ctx.channel().remoteAddress());
    switch (request.getCode()) {
      case RequestCode.HEADER_REQUEST:
        final RequestMessage headerMessage =
            (RequestMessage) request.decodeCommandCustomHeader(CommandCustomHeader.class);
        return processDtsMessage(clientIp, headerMessage);
      case RequestCode.BODY_REQUEST:
        final byte[] body = request.getBody();
        RequestMessage bodyMessage = RemotingSerializable.decode(body, RequestMessage.class);
        return processDtsMessage(clientIp, bodyMessage);
      default:
        break;
    }
    final RemotingCommand response = RemotingCommand
        .createResponseCommand(ResponseCode.REQUEST_CODE_NOT_SUPPORTED, "No request Code");
    return response;
  }

  private RemotingCommand processDtsMessage(String clientIp, RequestMessage dtsMessage) {
    RemotingCommand response = RemotingCommand.createResponseCommand(null);
    CommandCustomHeader responseHeader;
    try {
      if (dtsMessage instanceof BeginMessage) {
        // 开始一个分布式事务
        response = RemotingCommand.createResponseCommand(BeginResultMessage.class);
        responseHeader = response.readCustomHeader();
        createMessageHandler().handleMessage(clientIp, (BeginMessage) dtsMessage,
            (BeginResultMessage) responseHeader);
        response.setCode(ResponseCode.SUCCESS);
        return response;
      } else if (dtsMessage instanceof GlobalCommitMessage) {
        // 处理全局事务提交
        response = RemotingCommand.createResponseCommand(GlobalCommitResultMessage.class);
        responseHeader = response.readCustomHeader();
        createMessageHandler().handleMessage(clientIp, (GlobalCommitMessage) dtsMessage,
            (GlobalCommitResultMessage) responseHeader);
        response.setCode(ResponseCode.SUCCESS);
        return response;
      } else if (dtsMessage instanceof GlobalRollbackMessage) {
        // 处理全局事务回滚
        response = RemotingCommand.createResponseCommand(GlobalRollbackResultMessage.class);
        responseHeader = response.readCustomHeader();
        createMessageHandler().handleMessage(clientIp, (GlobalRollbackMessage) dtsMessage,
            (GlobalRollbackResultMessage) responseHeader);
        response.setCode(ResponseCode.SUCCESS);
        return response;
      } else if (dtsMessage instanceof RegisterMessage) {
        // 处理事务分支注册
        response = RemotingCommand.createResponseCommand(RegisterResultMessage.class);
        responseHeader = response.readCustomHeader();
        createMessageHandler().handleMessage(clientIp, (RegisterMessage) dtsMessage,
            (RegisterResultMessage) responseHeader);
        response.setCode(ResponseCode.SUCCESS);
        return response;
      }
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);
      response.setCode(ResponseCode.SYSTEM_ERROR);
      response.setRemark(e.getMessage());
      return response;
    }
    response.setCode(ResponseCode.REQUEST_CODE_NOT_SUPPORTED);
    response.setRemark("not found request message proccessor");
    return response;
  }


}
