package org.dts.server.processor;

import org.dts.server.utils.BeanFactoryUtil;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.ResponseCode;
import com.quancheng.dts.rpc.remoting.common.RemotingHelper;
import com.quancheng.dts.rpc.remoting.netty.NettyRequestProcessor;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guoyubo on 2017/8/29.
 */
public class DefaultRequestProcessor implements NettyRequestProcessor {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(DefaultRequestProcessor.class);


  @Override
  public RemotingCommand processRequest(final ChannelHandlerContext ctx, final RemotingCommand request)
      throws Exception {
      log.info("receive request, {} {} {}",
          request.getCode(),
          RemotingHelper.parseChannelRemoteAddr(ctx.channel()),
          request);

    BusinessProcessor processor = ((ProcessorFactory)BeanFactoryUtil.getService("processorFactory")).getProcessor(request.getCode());
    if (processor != null) {
      return  processor.handler(ctx, request);
    }

    final RemotingCommand response = RemotingCommand.createResponseCommand(null);
    response.setCode(ResponseCode.REQUEST_CODE_NOT_SUPPORTED);
    response.setRemark(null);
    return response;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }


}
