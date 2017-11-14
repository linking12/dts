package io.dts.client.api.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestMessage;
import io.dts.common.protocol.heatbeat.HeartbeatRequestHeader;
import io.dts.common.util.ThreadFactoryImpl;
import io.dts.remoting.RemoteConstant;
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
public class DefaultDtsClientMessageSender implements DtsClientMessageSender {

  private final RemotingClient remotingClient;
  private final ScheduledExecutorService scheduledExecutorService;

  public DefaultDtsClientMessageSender() {
    final NettyClientConfig nettyClientConfig = new NettyClientConfig();
    this.remotingClient = new NettyRemotingClient(nettyClientConfig, null);
    this.scheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("ClientHeadBet_"));
  }

  public void start() {
    this.remotingClient.start();
    this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          HeartbeatRequestHeader hearbeat = new HeartbeatRequestHeader();
          DefaultDtsClientMessageSender.this.invoke(hearbeat);
        } catch (Throwable e) {
          // ignore
        }
      }
    }, 0, 5, TimeUnit.SECONDS);
  }

  public void stop() {
    this.remotingClient.shutdown();
    this.scheduledExecutorService.shutdownNow();
  }

  @Override
  public <T> T invoke(RequestMessage msg, long timeout) throws DtsException {
    String serverAddress = selectAddress();
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
    return this.invoke(msg, RemoteConstant.RPC_INVOKE_TIMEOUT);
  }



}
