package io.dts.client.transport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.Collections;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.remoting.RemotingClient;
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
  public Object invoke(final Object msg, final long timeout) throws DtsException {
    if (DtsContext.getCurrentXid() != null) {
      return this.invoke(TxcXID.getServerAddress(DtsContext.getCurrentXid()), msg, timeout);
    } else {
      return this.invoke(null, msg, timeout);
    }
  }

  @Override
  public Object invoke(final String serverAddress, final Object msg, final long timeout)
      throws DtsException {
    try {
      return remotingClient.invokeSync(serverAddress, (RemotingCommand) msg, timeout);
    } catch (InterruptedException e) {
      throw new DtsException(e);
    } catch (RemotingConnectException e) {
      throw new DtsException(e);
    } catch (RemotingSendRequestException e) {
      throw new DtsException(e);
    } catch (RemotingTimeoutException e) {
      throw new DtsException(e);
    }
  }

  @Override
  public Object invoke(final Object msg) throws DtsException {
    return this.invoke(msg, 3000l);
  }

  @Override
  public void sendResponse(final long msgId, final String serverAddress, final Object msg) {
  }
}
