package io.dts.server.network.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.heatbeat.HeartbeatRequestHeader;
import io.dts.common.protocol.heatbeat.HeartbeatResponseHeader;
import io.dts.remoting.exception.RemotingCommandException;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSysResponseCode;
import io.dts.server.network.channel.ChannelInfo;
import io.dts.server.network.channel.ChannelRepository;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @author liushiming
 * @version ChannelHeatBeatProcessor.java, v 0.0.1 2017年9月6日 上午11:37:02 liushiming
 */
@Component
@Qualifier("heatBeatProcessor")
public class HeatBeatProcessor implements NettyRequestProcessor {

  @Autowired
  private ChannelRepository channelRepository;

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
        header.getClientOrResourceInfo(), //
        request.getLanguage(), //
        request.getVersion()//
    );
    channelRepository.registerChannel(clientChannelInfo);
    RemotingCommand response = RemotingCommand.createResponseCommand(HeartbeatResponseHeader.class);
    response.setCode(RemotingSysResponseCode.SUCCESS);
    return response;
  }
}
