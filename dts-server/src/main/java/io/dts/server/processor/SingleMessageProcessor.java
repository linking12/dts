package io.dts.server.processor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Queues;

import io.dts.common.ThreadFactoryImpl;
import io.dts.common.protocol.DtsMessage;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.ResponseCode;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.server.TcpServerController;
import io.dts.server.TcpServerProperties;
import io.dts.server.service.DtsServerMessageHandler;
import io.dts.util.NetUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @author liushiming
 * @version AddProcessor.java, v 0.0.1 2017年9月6日 上午11:36:12 liushiming
 */
@SuppressWarnings("unused")
public class SingleMessageProcessor implements NettyRequestProcessor {

  private static final Logger logger = LoggerFactory.getLogger(SingleMessageProcessor.class);

  private final ExecutorService addProcessorExecutor;

  private final TcpServerController serverController;

  private final DtsServerMessageHandler messageHandler;

  public SingleMessageProcessor(TcpServerController serverController,
      TcpServerProperties properties) {
    this.serverController = serverController;
    this.addProcessorExecutor = new ThreadPoolExecutor(//
        properties.getWriteThreadPoolQueueSize(), //
        properties.getWriteThreadPoolQueueSize(), //
        1000 * 60, //
        TimeUnit.MILLISECONDS, //
        Queues.newLinkedBlockingDeque(properties.getWriteThreadPoolQueueSize()), //
        new ThreadFactoryImpl("AddProcessorThread_"));
    this.messageHandler = new DtsServerMessageHandler();
  }

  public ExecutorService getThreadPool() {
    return addProcessorExecutor;
  }

  @Override
  public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
      throws Exception {
    final String clientIp = NetUtil.toStringAddress(ctx.channel().remoteAddress());
    switch (request.getCode()) {
      case RequestCode.REQUEST_CODE:
        final DtsMessage dtsMessage =
            (DtsMessage) request.decodeCommandCustomHeader(DtsMessage.class);
        return processDtsMessage(clientIp, dtsMessage);
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
      case DtsMessage.TYPE_BEGIN:
        response = RemotingCommand.createResponseCommand(BeginResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (BeginMessage) dtsMessage,
            (BeginResultMessage) responseHeader);
        return response;
      case DtsMessage.TYPE_REGIST:
        response = RemotingCommand.createResponseCommand(RegisterResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (RegisterMessage) dtsMessage,
            (RegisterResultMessage) responseHeader);
        return response;
      case DtsMessage.TYPE_GLOBAL_COMMIT:
        response = RemotingCommand.createResponseCommand(GlobalCommitResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (GlobalCommitMessage) dtsMessage,
            (GlobalCommitResultMessage) responseHeader);
        return response;
      case DtsMessage.TYPE_GLOBAL_ROLLBACK:
        response = RemotingCommand.createResponseCommand(BranchRollbackResultMessage.class);
        responseHeader = response.readCustomHeader();
        messageHandler.handleMessage(clientIp, (GlobalRollbackMessage) dtsMessage,
            (BranchRollbackResultMessage) responseHeader);
        return response;



      default:
        break;
    }



    return null;
  }

}
