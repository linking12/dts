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
package io.dts.server.remoting.processor;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.dts.common.api.DtsServerMessageHandler;
import io.dts.common.protocol.DtsMessage;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.RequestHeaderMessage;
import io.dts.common.protocol.ResponseCode;
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
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;
import io.dts.util.NetUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author liushiming
 * @version MessageProcessorComponent.java, v 0.0.1 2017年9月13日 下午2:00:41 liushiming
 */
@Component
@Qualifier("dtsMessageProcessor")
@Scope("prototype")
public class DtsMessageProcessor implements NettyRequestProcessor {

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
        final RequestHeaderMessage headerMessage =
            (RequestHeaderMessage) request.decodeCommandCustomHeader(RequestHeaderMessage.class);
        return processDtsMessage(clientIp, headerMessage);
      case RequestCode.BODY_REQUEST:
        final byte[] body = request.getBody();
        DtsMessage bodyMessage = RemotingSerializable.decode(body, DtsMessage.class);
        return processDtsMessage(clientIp, bodyMessage);
      default:
        break;
    }
    final RemotingCommand response = RemotingCommand
        .createResponseCommand(ResponseCode.REQUEST_CODE_NOT_SUPPORTED, "No request Code");
    return response;
  }

  private RemotingCommand processDtsMessage(String clientIp, DtsMessage dtsMessage) {
    short typeCode = dtsMessage.getTypeCode();
    RemotingCommand response = RemotingCommand.createResponseCommand(null);
    CommandCustomHeader responseHeader;
    try {
      switch (typeCode) {
        // 开始一个分布式事务
        case DtsMessage.TYPE_BEGIN:
          response = RemotingCommand.createResponseCommand(BeginResultMessage.class);
          responseHeader = response.readCustomHeader();
          createMessageHandler().handleMessage(clientIp, (BeginMessage) dtsMessage,
              (BeginResultMessage) responseHeader);
          response.setCode(ResponseCode.SUCCESS);
          return response;
        // 处理全局事务提交
        case DtsMessage.TYPE_GLOBAL_COMMIT:
          response = RemotingCommand.createResponseCommand(GlobalCommitResultMessage.class);
          responseHeader = response.readCustomHeader();
          createMessageHandler().handleMessage(clientIp, (GlobalCommitMessage) dtsMessage,
              (GlobalCommitResultMessage) responseHeader);
          return response;
        // 处理全局事务回滚
        case DtsMessage.TYPE_GLOBAL_ROLLBACK:
          response = RemotingCommand.createResponseCommand(BranchRollbackResultMessage.class);
          responseHeader = response.readCustomHeader();
          createMessageHandler().handleMessage(clientIp, (GlobalRollbackMessage) dtsMessage,
              (GlobalRollbackResultMessage) responseHeader);
          response.setCode(ResponseCode.SUCCESS);
          return response;
        // 处理事务分支注册
        case DtsMessage.TYPE_REGIST:
          response = RemotingCommand.createResponseCommand(RegisterResultMessage.class);
          responseHeader = response.readCustomHeader();
          createMessageHandler().handleMessage(clientIp, (RegisterMessage) dtsMessage,
              (RegisterResultMessage) responseHeader);
          response.setCode(ResponseCode.SUCCESS);
          return response;
        // 事务分支上报状态消息处理
        case DtsMessage.TYPE_REPORT_STATUS:
          response = RemotingCommand.createResponseCommand(ReportStatusResultMessage.class);
          responseHeader = response.readCustomHeader();
          createMessageHandler().handleMessage(clientIp, (ReportStatusMessage) dtsMessage,
              (ReportStatusResultMessage) responseHeader);
          response.setCode(ResponseCode.SUCCESS);
          return response;
        // 可重试事务分支处理
        case DtsMessage.TYPE_BEGIN_RETRY_BRANCH_RESULT:
          response = RemotingCommand.createResponseCommand(BeginRetryBranchResultMessage.class);
          responseHeader = response.readCustomHeader();
          createMessageHandler().handleMessage(clientIp, (BeginRetryBranchMessage) dtsMessage,
              (BeginRetryBranchResultMessage) responseHeader);
          response.setCode(ResponseCode.SUCCESS);
          return response;
        // 事务分支上报用户数据（udata）消息处理
        case DtsMessage.TYPE_REPORT_UDATA_RESULT:
          response = RemotingCommand.createResponseCommand(ReportUdataResultMessage.class);
          responseHeader = response.readCustomHeader();
          createMessageHandler().handleMessage(clientIp, (ReportUdataMessage) dtsMessage,
              (ReportUdataResultMessage) responseHeader);
          response.setCode(ResponseCode.SUCCESS);
          return response;
        // 合并消息的处理
        case DtsMessage.TYPE_DTS_MERGE:
          DtsMultipleResonseMessage responseMessage = new DtsMultipleResonseMessage();
          createMessageHandler().handleMessage(clientIp, (DtsMultipleRequestMessage) dtsMessage,
              responseMessage);
          response.setCode(ResponseCode.SUCCESS);
          response.setBody(responseMessage.encode());
          return response;
        // 查询锁是否占用消息处理
        case DtsMessage.TYPE_QUERY_LOCK:
          response = RemotingCommand.createResponseCommand(QueryLockResultMessage.class);
          responseHeader = response.readCustomHeader();
          createMessageHandler().handleMessage(clientIp, (QueryLockMessage) dtsMessage,
              (QueryLockResultMessage) responseHeader);
          response.setCode(ResponseCode.SUCCESS);
          return response;
        default:
          break;
      }
    } catch (Throwable e) {
      response.setCode(ResponseCode.SYSTEM_ERROR);
      response.setRemark(e.getMessage());
      return response;
    }
    response.setCode(ResponseCode.REQUEST_CODE_NOT_SUPPORTED);
    response.setRemark("not found request message proccessor");
    return response;
  }


}
