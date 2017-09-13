package io.dts.server.remoting.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.protocol.DtsMessage;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.RequestHeaderMessage;
import io.dts.common.protocol.ResponseCode;
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
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;
import io.dts.remoting.protocol.RemotingSysResponseCode;
import io.dts.server.remoting.DtsServerController;
import io.dts.server.service.DtsServerMessageHandler;
import io.dts.util.NetUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @author liushiming
 * @version AddProcessor.java, v 0.0.1 2017年9月6日 上午11:36:12 liushiming
 */
@SuppressWarnings("unused")
public class ServerMessageProcessor implements NettyRequestProcessor {

  private static final Logger logger = LoggerFactory.getLogger(ServerMessageProcessor.class);

  private final DtsServerController serverController;

  private final DtsServerMessageHandler messageHandler;

  public ServerMessageProcessor(DtsServerController serverController) {
    this.serverController = serverController;
    this.messageHandler = new DtsServerMessageHandler();
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
    RemotingCommand response;
    CommandCustomHeader responseHeader;
    switch (typeCode) {
      // 开始一个分布式事务
      case DtsMessage.TYPE_BEGIN:
        response = RemotingCommand.createResponseCommand(BeginResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (BeginMessage) dtsMessage,
            (BeginResultMessage) responseHeader);
        return response;
      // 处理全局事务提交
      case DtsMessage.TYPE_GLOBAL_COMMIT:
        response = RemotingCommand.createResponseCommand(GlobalCommitResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (GlobalCommitMessage) dtsMessage,
            (GlobalCommitResultMessage) responseHeader);
        return response;
      // 处理全局事务回滚
      case DtsMessage.TYPE_GLOBAL_ROLLBACK:
        response = RemotingCommand.createResponseCommand(BranchRollbackResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (GlobalRollbackMessage) dtsMessage,
            (GlobalRollbackResultMessage) responseHeader);
        return response;
      // 处理事务分支注册
      case DtsMessage.TYPE_REGIST:
        response = RemotingCommand.createResponseCommand(RegisterResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (RegisterMessage) dtsMessage,
            (RegisterResultMessage) responseHeader);
        return response;
      // 事务分支上报状态消息处理
      case DtsMessage.TYPE_REPORT_STATUS:
        response = RemotingCommand.createResponseCommand(ReportStatusResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (ReportStatusMessage) dtsMessage,
            (ReportStatusResultMessage) responseHeader);
        return response;
      // 可重试事务分支处理
      case DtsMessage.TYPE_BEGIN_RETRY_BRANCH_RESULT:
        response = RemotingCommand.createResponseCommand(BeginRetryBranchResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (BeginRetryBranchMessage) dtsMessage,
            (BeginRetryBranchResultMessage) responseHeader);
        return response;
      // 事务分支上报用户数据（udata）消息处理
      case DtsMessage.TYPE_REPORT_UDATA_RESULT:
        response = RemotingCommand.createResponseCommand(ReportUdataResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (ReportUdataMessage) dtsMessage,
            (ReportUdataResultMessage) responseHeader);
        return response;
      // 合并消息的处理
      case DtsMessage.TYPE_DTS_MERGE:
        response = RemotingCommand.createResponseCommand(null);
        DtsMultipleResonseMessage responseMessage = new DtsMultipleResonseMessage();
        messageHandler.handleMessage(clientIp, (DtsMultipleRequestMessage) dtsMessage,
            responseMessage);
        response.setCode(RemotingSysResponseCode.SUCCESS);
        response.setBody(responseMessage.encode());
        return response;
      // 查询锁是否占用消息处理
      case DtsMessage.TYPE_QUERY_LOCK:
        response = RemotingCommand.createResponseCommand(QueryLockResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (QueryLockMessage) dtsMessage,
            (QueryLockResultMessage) responseHeader);
        return response;
      // 处理事务分支提交的反馈结果
      case DtsMessage.TYPE_BRANCH_COMMIT_RESULT:
        response = RemotingCommand.createResponseCommand(null);
        messageHandler.handleMessage(clientIp, (BranchCommitResultMessage) dtsMessage);
        response.setCode(RemotingSysResponseCode.SUCCESS);
        return response;
      case DtsMessage.TYPE_BRANCH_ROLLBACK_RESULT:
        response = RemotingCommand.createResponseCommand(null);
        messageHandler.handleMessage(clientIp, (BranchRollbackResultMessage) dtsMessage);
        response.setCode(RemotingSysResponseCode.SUCCESS);
        return response;
      default:
        break;
    }
    return null;
  }

}
