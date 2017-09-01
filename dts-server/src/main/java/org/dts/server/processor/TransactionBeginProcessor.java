package org.dts.server.processor;

import org.dts.server.entity.DtsTransaction;
import org.dts.server.service.DtsTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.ResponseCode;
import com.quancheng.dts.common.DtsXID;
import com.quancheng.dts.message.request.BeginMessage;
import com.quancheng.dts.message.response.BeginResultMessage;
import com.quancheng.dts.rpc.remoting.common.RemotingHelper;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

import java.util.Calendar;
import java.util.HashMap;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guoyubo on 2017/8/31.
 */
@Component
public class TransactionBeginProcessor implements BusinessProcessor {

  @Autowired
  DtsTransactionService transactionService;

  @Override
  public RemotingCommand handler(final ChannelHandlerContext ctx, final RemotingCommand request) {
    final RemotingCommand response = RemotingCommand.createResponseCommand(null);
    HashMap<String, String> extFields = request.getExtFields();
    RemotingHelper.parseChannelRemoteAddr(ctx.channel());
    DtsTransaction dtsTransaction = transactionService
        .createTransaction(RemotingSerializable.decode(request.getBody(), BeginMessage.class), extFields);
    BeginResultMessage beginResultMessage = new BeginResultMessage();
    beginResultMessage.setXid(DtsXID.generateXID(dtsTransaction.getId()));
    beginResultMessage.setNextServerAddr(DtsXID.getSvrAddr());
    response.setBody(RemotingSerializable.encode(beginResultMessage));
    response.setCode(ResponseCode.SUCCESS);
    response.setRemark(null);
    return response;
  }

}
