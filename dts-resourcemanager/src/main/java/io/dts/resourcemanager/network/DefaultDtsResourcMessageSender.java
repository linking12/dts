package io.dts.resourcemanager.network;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Queues;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestCode;
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
import io.dts.resourcemanager.ResourceManager;
import io.dts.resourcemanager.network.processor.RmMessageProcessor;

/**
 * Created by guoyubo on 2017/9/13.
 */
public class DefaultDtsResourcMessageSender implements DtsClientMessageSender {

  private final RemotingClient remotingClient;
  private final ScheduledExecutorService scheduledExecutorService;
  private final NettyClientConfig nettyClientConfig;
  private static DefaultDtsResourcMessageSender resourceManagerSender =
      new DefaultDtsResourcMessageSender();
  private ResourceManager rm;

  private DefaultDtsResourcMessageSender() {
    this.nettyClientConfig = new NettyClientConfig();
    this.remotingClient = new NettyRemotingClient(nettyClientConfig, null);
    this.scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(new ThreadFactoryImpl("DtsResourceManager Heartbeat"));
  }

  public static final DefaultDtsResourcMessageSender getInstance() {
    return resourceManagerSender;
  }

  public void registerResourceManager(ResourceManager rm) {
    this.rm = rm;
    registerHeaderRequest(rm);
    registerBodyRequest(rm);
  }

  private void registerHeaderRequest(ResourceManager rm) {
    RmMessageProcessor messageProcessor = new RmMessageProcessor(rm);
    BlockingQueue<Runnable> clientThreadPoolQueue = Queues.newLinkedBlockingDeque(100);
    ExecutorService clientMessageExecutor =
        new ThreadPoolExecutor(nettyClientConfig.getClientCallbackExecutorThreads(),
            nettyClientConfig.getClientCallbackExecutorThreads(), 1000 * 60, TimeUnit.MILLISECONDS,
            clientThreadPoolQueue, new ThreadFactoryImpl("RMThread_"));
    this.remotingClient.registerProcessor(RequestCode.HEADER_REQUEST, messageProcessor,
        clientMessageExecutor);
  }

  private void registerBodyRequest(ResourceManager rm) {
    RmMessageProcessor messageProcessor = new RmMessageProcessor(rm);
    BlockingQueue<Runnable> clientThreadPoolQueue = Queues.newLinkedBlockingDeque(100);
    ExecutorService clientMessageExecutor =
        new ThreadPoolExecutor(nettyClientConfig.getClientCallbackExecutorThreads(),
            nettyClientConfig.getClientCallbackExecutorThreads(), 1000 * 60, TimeUnit.MILLISECONDS,
            clientThreadPoolQueue, new ThreadFactoryImpl("RMThread_"));
    this.remotingClient.registerProcessor(RequestCode.BODY_REQUEST, messageProcessor,
        clientMessageExecutor);
  }

  public void start() {
    this.remotingClient.start();
    this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          HeartbeatRequestHeader hearbeat = new HeartbeatRequestHeader();
          hearbeat.setClientOrResourceInfo(rm.getRegisterKey());
          DefaultDtsResourcMessageSender.this.invoke(hearbeat);
        } catch (Throwable e) {
          e.printStackTrace();
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
