package io.dts.resourcemanager.remoting.sender;

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestMessage;
import io.dts.remoting.RemotingClient;
import io.dts.remoting.exception.RemotingCommandException;
import io.dts.remoting.exception.RemotingConnectException;
import io.dts.remoting.exception.RemotingSendRequestException;
import io.dts.remoting.exception.RemotingTimeoutException;
import io.dts.remoting.netty.NettyClientConfig;
import io.dts.remoting.netty.NettyRemotingClient;
import io.dts.remoting.protocol.RemotingCommand;

/**
 * Created by guoyubo on 2017/9/13.
 */
public class DtsClientMessageSenderImpl implements DtsClientMessageSender {


  private RemotingClient remotingClient;

  public DtsClientMessageSenderImpl(NettyClientConfig nettyClientConfig) {
    this.remotingClient = new NettyRemotingClient(nettyClientConfig);
    this.remotingClient.updateNameServerAddressList(Collections.singletonList("127.0.0.1:10086"));
  }

  @PostConstruct
  public void init() {
    remotingClient.start();
  }

  @PreDestroy
  public void destroy() {
    remotingClient.shutdown();
  }

  @Override
  public <T> T invoke(RequestMessage msg, long timeout) throws DtsException {
    if (DtsContext.getCurrentXid() != null) {
      return this.invoke(TxcXID.getServerAddress(DtsContext.getCurrentXid()), msg, timeout);
    } else {
      return this.invoke(null, msg, timeout);
    }
  }

  @Override
  public <T> T invoke(String serverAddress, RequestMessage msg, long timeout) throws DtsException {
    RemotingCommand request = this.buildRequest(msg);
    try {
      RemotingCommand response = remotingClient.invokeSync(serverAddress, request, timeout);
      return this.buildResponse(response);
    } catch (RemotingConnectException | RemotingSendRequestException | RemotingTimeoutException
        | InterruptedException | RemotingCommandException e) {
      throw new DtsException(e);
    }
  }

  @Override
  public <T> T invoke(RequestMessage msg) throws DtsException {
    return this.invoke(msg, 3000l);
  }
}
