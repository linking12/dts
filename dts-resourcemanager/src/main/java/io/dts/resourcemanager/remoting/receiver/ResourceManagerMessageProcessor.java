package io.dts.resourcemanager.remoting.receiver;

import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guoyubo on 2017/9/15.
 */
public class ResourceManagerMessageProcessor implements NettyRequestProcessor {

  @Override
  public RemotingCommand processRequest(final ChannelHandlerContext ctx, final RemotingCommand request)
      throws Exception {
    return null;
  }
}
