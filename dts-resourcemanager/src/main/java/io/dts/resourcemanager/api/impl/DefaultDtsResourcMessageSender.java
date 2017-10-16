package io.dts.resourcemanager.api.impl;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.dts.common.common.TxcConstants;
import io.dts.common.common.exception.DtsException;
import io.dts.common.component.AbstractLifecycleComponent;
import io.dts.common.protocol.RequestMessage;
import io.dts.common.protocol.heatbeat.HeartbeatRequestHeader;
import io.dts.common.rpc.DtsClientMessageSender;
import io.dts.common.util.thread.ThreadFactoryImpl;
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
public class DefaultDtsResourcMessageSender extends AbstractLifecycleComponent
    implements DtsClientMessageSender {

  private final RemotingClient remotingClient;
  private final ScheduledExecutorService scheduledExecutorService;
  private final String serverAddress;

  public DefaultDtsResourcMessageSender(final String serverAddress) {
    final NettyClientConfig nettyClientConfig = new NettyClientConfig();
    this.remotingClient = new NettyRemotingClient(nettyClientConfig, null);
    this.scheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("DtsClient Heartbeat"));
    this.serverAddress = serverAddress;
  }

  @Override
  protected void doStart() {
    this.remotingClient.start();
    this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          HeartbeatRequestHeader hearbeat = new HeartbeatRequestHeader();
          DefaultDtsResourcMessageSender.this.invoke(hearbeat);
        } catch (Throwable e) {
          // ignore
        }
      }
    }, 5, 5, TimeUnit.SECONDS);
  }

  @Override
  protected void doStop() {
    this.remotingClient.shutdown();
    this.scheduledExecutorService.shutdownNow();
  }

  @Override
  public <T> T invoke(RequestMessage msg, long timeout) throws DtsException {
    return this.invoke(serverAddress, msg, timeout);
  }

  @Override
  public <T> T invoke(String serverAddress, RequestMessage msg, long timeout) throws DtsException {
    try {
      RemotingCommand request = this.buildRequest(msg);
      RemotingCommand response = remotingClient.invokeSync(serverAddress, request, timeout);
      return this.buildResponse(response);
    } catch (RemotingCommandException | RemotingConnectException | RemotingSendRequestException
        | RemotingTimeoutException | InterruptedException e) {
      throw new DtsException(e);
    }
  }

  @Override
  public <T> T invoke(RequestMessage msg) throws DtsException {
    return this.invoke(msg, TxcConstants.RPC_INVOKE_TIMEOUT);
  }

  @Override
  protected void doClose() throws IOException {

  }



}
