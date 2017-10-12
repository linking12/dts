package io.dts.resourcemanager.remoting;

import com.google.common.collect.Queues;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.dts.common.ThreadFactoryImpl;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.heatbeat.HeartbeatRequestHeader;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.RemotingClient;
import io.dts.remoting.exception.RemotingConnectException;
import io.dts.remoting.exception.RemotingSendRequestException;
import io.dts.remoting.exception.RemotingTimeoutException;
import io.dts.remoting.netty.NettyClientConfig;
import io.dts.remoting.netty.NettyRemotingClient;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.resourcemanager.remoting.listener.RmMessageProcessor;

/**
 * Created by guoyubo on 2017/9/20.
 */
public class DtsRemotingClient {

  private RemotingClient remotingClient;

  private ScheduledExecutorService scheduledExecutorService;

  public DtsRemotingClient(NettyClientConfig nettyClientConfig, final List<String> serverAddressList) {
    this.remotingClient = new NettyRemotingClient(nettyClientConfig);
    this.remotingClient.updateNameServerAddressList(serverAddressList);
    NettyRequestProcessor messageProcessor = new RmMessageProcessor();
    BlockingQueue<Runnable> clientThreadPoolQueue =
        Queues.newLinkedBlockingDeque(100);
    ExecutorService clientMessageExecutor =
        new ThreadPoolExecutor(nettyClientConfig.getClientCallbackExecutorThreads(),
            nettyClientConfig.getClientCallbackExecutorThreads(), 1000 * 60, TimeUnit.MILLISECONDS,
            clientThreadPoolQueue, new ThreadFactoryImpl("ResourceMessageThread_"));
    this.remotingClient.registerProcessor(RequestCode.HEADER_REQUEST, messageProcessor,
        clientMessageExecutor);
    this.remotingClient.registerProcessor(RequestCode.BODY_REQUEST, messageProcessor,
        clientMessageExecutor);
    scheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("Dts RM Heartbeat"));

  }

  private void sendHeartbeatToServer() {

    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          HeartbeatRequestHeader header = new HeartbeatRequestHeader();
          RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.HEART_BEAT,
              (CommandCustomHeader) header);
          remotingClient.invokeSync(null, request, 1000 * 3);
        } catch (Exception e) {
          // ignore
        }
      }
    }, 0, 5, TimeUnit.SECONDS);
  }

  @PostConstruct
  public void start() {
    remotingClient.start();
    sendHeartbeatToServer();
  }

  @PreDestroy
  public void shutdown() {
    scheduledExecutorService.shutdownNow();
    remotingClient.shutdown();
  }

  public RemotingCommand invokeSync(final String serverAddress, final RemotingCommand request, final long timeout){
    try {
      return remotingClient.invokeSync(serverAddress, request, timeout);
    } catch (InterruptedException e) {
      throw new DtsException(e, "internal error");
    } catch (RemotingConnectException e) {
      throw new DtsException(e, "connect remote server error");
    } catch (RemotingSendRequestException e) {
      throw new DtsException(e, "send request error");
    } catch (RemotingTimeoutException e) {
      throw new DtsException(e, "remote timeout");
    }
  }
}
