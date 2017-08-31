package org.dts.server.processor;

import org.slf4j.LoggerFactory;

import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.RequestCode;
import com.quancheng.dts.ResponseCode;
import com.quancheng.dts.common.DtsXID;
import com.quancheng.dts.message.response.TransactionBeginBody;
import com.quancheng.dts.rpc.remoting.common.RemotingHelper;
import com.quancheng.dts.rpc.remoting.netty.NettyRequestProcessor;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

import java.util.Calendar;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guoyubo on 2017/8/29.
 */
public class DefaultRequestProcessor implements NettyRequestProcessor {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(DefaultRequestProcessor.class);


  @Override
  public RemotingCommand processRequest(final ChannelHandlerContext ctx, final RemotingCommand request)
      throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("receive request, {} {} {}",
          request.getCode(),
          RemotingHelper.parseChannelRemoteAddr(ctx.channel()),
          request);
    }
    System.out.println(request);
    switch (request.getCode()) {
      case RequestCode.TRANSACTION_BEGIN:
        return this.createTransactionBeginCommand(ctx, request);
      default:
        break;
    }
    return null;
  }

  private RemotingCommand createTransactionBeginCommand(final ChannelHandlerContext ctx, final RemotingCommand request) {
    final RemotingCommand response = RemotingCommand.createResponseCommand(null);
//    final PutKVConfigRequestHeader requestHeader =
//        (PutKVConfigRequestHeader) request.decodeCommandCustomHeader(PutKVConfigRequestHeader.class);

    TransactionBeginBody transactionBeginBody = new TransactionBeginBody();
    transactionBeginBody.setXid(DtsXID.generateXID(Calendar.getInstance().getTimeInMillis()));//TODO
    transactionBeginBody.setNextServerAddr(DtsXID.getSvrAddr());
    response.setBody(RemotingSerializable.encode(transactionBeginBody));
    response.setCode(ResponseCode.SUCCESS);
    response.setRemark(null);
    return response;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }


}
