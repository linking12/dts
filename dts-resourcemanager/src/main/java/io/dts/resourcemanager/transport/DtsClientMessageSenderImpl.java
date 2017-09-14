package io.dts.resourcemanager.transport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
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

  public DtsClientMessageSenderImpl() {
    final NettyClientConfig nettyClientConfig = new NettyClientConfig();
    this.remotingClient = new NettyRemotingClient(nettyClientConfig);
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
  public Object invoke(final Object msg, final long timeout) throws IOException, TimeoutException {
    return this.invoke(TxcXID.getServerAddress(DtsContext.getCurrentXid()), msg, timeout);
  }

  @Override
  public Object invoke(final String serverAddress, final Object msg, final long timeout)
      throws IOException, TimeoutException {
    try {
      return remotingClient.invokeSync(serverAddress, (RemotingCommand) msg, timeout);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (RemotingConnectException e) {
      e.printStackTrace();
    } catch (RemotingSendRequestException e) {
      e.printStackTrace();
    } catch (RemotingTimeoutException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Object invoke(final Object msg) throws IOException, TimeoutException {
    return this.invoke(msg, 3000l);
  }

  @Override
  public void sendResponse(final long msgId, final String serverAddress, final Object msg) {
  }
}
