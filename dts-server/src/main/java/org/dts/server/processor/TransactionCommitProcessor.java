package org.dts.server.processor;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.ResponseCode;
import com.quancheng.dts.message.request.GlobalCommitMessage;
import com.quancheng.dts.message.response.GlobalCommitResultMessage;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by guoyubo on 2017/8/31.
 */
@Component
public class TransactionCommitProcessor implements BusinessProcessor {


  @Override
  public RemotingCommand handler(final ChannelHandlerContext ctx, final RemotingCommand request) {
    final RemotingCommand response = RemotingCommand.createResponseCommand(null);
    GlobalCommitMessage
        commitMessage = RemotingSerializable.decode(request.getBody(), GlobalCommitMessage.class);

    GlobalCommitResultMessage commitResultMessage = new GlobalCommitResultMessage();
    commitResultMessage.setTranId(commitMessage.getTranId());
    commitResultMessage.setResult(1);
    commitResultMessage.setMsg("Success Commit");
    response.setBody(RemotingSerializable.encode(commitResultMessage));
    response.setCode(ResponseCode.SUCCESS);
    response.setRemark(null);
    return response;
  }

}
