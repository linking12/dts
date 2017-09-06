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
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.server.TcpServerController;
import io.dts.server.TcpServerProperties;
import io.dts.server.service.DtsServerMessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.NetUtil;

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
    switch (request.getCode()) {
      case RequestCode.REQUEST_CODE:
        final DtsMessage dtsMessage =
            (DtsMessage) request.decodeCommandCustomHeader(DtsMessage.class);
        final String ipAndPort = NetUtil.toStringAddress(ctx.channel().remoteAddress());
        final String clientAppName = ipAndPortToClientAppName.get(ipAndPort);
        final String dbKeys = ipAndPortToDbKey.get(ipAndPort);
        return processDtsMessage(dtsMessage);
      default:
        break;
    }
    final RemotingCommand response = RemotingCommand
        .createResponseCommand(ResponseCode.REQUEST_CODE_NOT_SUPPORTED, "No request Code");
    return response;
  }


  private RemotingCommand processDtsMessage(DtsMessage dtsMessage) {
    short typeCode = dtsMessage.getTypeCode();
    switch (typeCode) {
      case DtsMessage.TYPE_BEGIN:
        messageHandler.handleMessage(msgId, dbKeys, clientIp, clientAppName, message, results, idx);
        break;

      default:
        break;
    }
    return null;
  }

}
