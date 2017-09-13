/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.dts.server.remoting;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Queues;

import io.dts.common.ThreadFactoryImpl;
import io.dts.common.protocol.RequestCode;
import io.dts.remoting.RemotingServer;
import io.dts.remoting.netty.NettyRemotingServer;
import io.dts.remoting.netty.NettyRequestProcessor;
import io.dts.remoting.netty.NettyServerConfig;
import io.dts.server.remoting.channel.ChannelHeatBeatProcessor;
import io.dts.server.remoting.channel.ChannelRepository;
import io.dts.server.remoting.channel.ChannelkeepingListener;
import io.dts.server.remoting.latency.ServerFixedThreadPoolExecutor;
import io.dts.server.remoting.processor.ServerMessageProcessor;

/**
 * @author liushiming
 * @version TcpServerController.java, v 0.0.1 2017年9月6日 上午10:06:17 liushiming
 */
@Component
public class DtsServerController {

  @Autowired
  private DtsServerProperties tcpServerProperties;

  private final ChannelRepository channelRepository;

  private final ChannelkeepingListener channelKeepingListener;

  private RemotingServer remotingServer;

  private final ExecutorService clientMessageExecutor;

  private final ExecutorService resourceMessageExecutor;

  private final ExecutorService channelHeatBeatProcessorExecutor;

  public DtsServerController() {
    this.channelRepository = ChannelRepository.newChannelRepository();
    this.channelKeepingListener = ChannelkeepingListener.newChannelkeepingListener(this);
    BlockingQueue<Runnable> clientThreadPoolQueue =
        Queues.newLinkedBlockingDeque(tcpServerProperties.getClientThreadPoolQueueSize());
    this.clientMessageExecutor =
        new ServerFixedThreadPoolExecutor(tcpServerProperties.getClientThreadPoolSize(),
            tcpServerProperties.getClientThreadPoolSize(), 1000 * 60, TimeUnit.MILLISECONDS,
            clientThreadPoolQueue, new ThreadFactoryImpl("ClientMessageThread_"));
    BlockingQueue<Runnable> resourceThreadPoolQueue =
        Queues.newLinkedBlockingDeque(tcpServerProperties.getResourceThreadPoolQueueSize());
    this.resourceMessageExecutor =
        new ServerFixedThreadPoolExecutor(tcpServerProperties.getResourceThreadPoolSize(),
            tcpServerProperties.getResourceThreadPoolSize(), 1000 * 60, TimeUnit.MILLISECONDS,
            resourceThreadPoolQueue, new ThreadFactoryImpl("ResourceMessageThread_"));
    this.channelHeatBeatProcessorExecutor =
        Executors.newFixedThreadPool(tcpServerProperties.getChannelHeatThreadPoolSize(),
            new ThreadFactoryImpl("ClientManageThread_"));
  }

  @PostConstruct
  public void init() {
    NettyServerConfig nettyServerConfig = new NettyServerConfig();
    nettyServerConfig.setListenPort(tcpServerProperties.getListenPort());
    this.remotingServer = new NettyRemotingServer(nettyServerConfig, channelKeepingListener);
    this.registerProcessor();
  }

  private void registerProcessor() {
    ServerMessageProcessor serverMessageProcessor = new ServerMessageProcessor(this);
    ChannelHeatBeatProcessor clientManageProcessor = new ChannelHeatBeatProcessor(this);
    registerProcessor(RequestCode.HEADER_REQUEST, serverMessageProcessor, clientMessageExecutor);
    registerProcessor(RequestCode.BODY_REQUEST, serverMessageProcessor, resourceMessageExecutor);
    registerProcessor(RequestCode.HEART_BEAT, clientManageProcessor,
        channelHeatBeatProcessorExecutor);
  }

  private void registerProcessor(int processorCode, NettyRequestProcessor processor,
      ExecutorService processorThreadPool) {
    this.remotingServer.registerProcessor(processorCode, processor, processorThreadPool);
  }

  public void start() {
    if (this.remotingServer != null) {
      this.remotingServer.start();
    }
    if (this.channelKeepingListener != null) {
      this.channelKeepingListener.start();
    }
  }

  @PreDestroy
  public void shutdown() {
    if (this.channelKeepingListener != null) {
      this.channelKeepingListener.shutdown();
    }
    if (this.remotingServer != null) {
      this.remotingServer.shutdown();
    }
  }

  public final ChannelRepository getChannelRepository() {
    return channelRepository;
  }

}
