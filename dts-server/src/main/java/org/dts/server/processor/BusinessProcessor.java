package org.dts.server.processor;

import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guoyubo on 2017/8/31.
 */
public interface BusinessProcessor<T> {

  RemotingCommand handler(final ChannelHandlerContext ctx, final RemotingCommand request);
}
