package io.dts.server.channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.dts.common.ThreadFactoryImpl;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.heatbeat.HeartbeatRequestHeader;
import io.dts.common.protocol.heatbeat.HeartbeatResponseHeader;
import io.dts.remoting.exception.RemotingCommandException;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSysResponseCode;
import io.dts.server.TcpServerController;
import io.dts.server.TcpServerProperties;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @author liushiming
 * @version ChannelHeatBeatProcessor.java, v 0.0.1 2017年9月6日 上午11:37:02 liushiming
 */
public class ChannelHeatBeatProcessor implements NettyRequestProcessor {


  private final TcpServerController serverController;

  private final ExecutorService channelHeatBeatProcessorExecutor;

  public ChannelHeatBeatProcessor(TcpServerController serverController,
      TcpServerProperties properties) {
    this.serverController = serverController;
    this.channelHeatBeatProcessorExecutor = Executors.newFixedThreadPool(
        properties.getChannelHeatThreadPoolSize(), new ThreadFactoryImpl("ClientManageThread_"));
  }

  public ExecutorService getThreadPool() {
    return this.channelHeatBeatProcessorExecutor;
  }

  @Override
  public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
      throws Exception {
    switch (request.getCode()) {
      case RequestCode.HEART_BEAT:
        return this.heartbeat(ctx, request);
      default:
        break;
    }
    return null;
  }

  private RemotingCommand heartbeat(ChannelHandlerContext ctx, RemotingCommand request)
      throws RemotingCommandException {
    HeartbeatRequestHeader header =
        (HeartbeatRequestHeader) request.decodeCommandCustomHeader(HeartbeatRequestHeader.class);
    ChannelInfo clientChannelInfo = new ChannelInfo(//
        ctx.channel(), //
        header.getClientId(), //
        request.getLanguage(), //
        request.getVersion()//
    );
    serverController.getChannelRepository().registerChannel("DEFAULT", clientChannelInfo);
    RemotingCommand response = RemotingCommand.createResponseCommand(HeartbeatResponseHeader.class);
    response.setCode(RemotingSysResponseCode.SUCCESS);
    return response;
  }
}
