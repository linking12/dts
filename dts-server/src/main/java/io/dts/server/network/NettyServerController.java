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
package io.dts.server.network;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Queues;

import io.dts.common.cluster.ServerCluster;
import io.dts.common.context.DtsXID;
import io.dts.common.protocol.RequestCode;
import io.dts.common.util.NetUtil;
import io.dts.common.util.ThreadFactoryImpl;
import io.dts.remoting.RemotingServer;
import io.dts.remoting.netty.NettyRemotingServer;
import io.dts.remoting.netty.NettyServerConfig;
import io.dts.server.network.channel.ChannelkeepingComponent;
import io.dts.server.network.processor.DtsMessageProcessor;
import io.dts.server.network.processor.HeatBeatProcessor;

/**
 * @author liushiming
 * @version DtsServerControllerComponent.java, v 0.0.1 2017年9月13日 下午1:58:27 liushiming
 */
@Component
public class NettyServerController {

  @Autowired
  private ChannelkeepingComponent channelKeeping;

  @Autowired
  @Qualifier("heatBeatProcessor")
  private HeatBeatProcessor heatBeatProccessor;

  @Value("${tcp.listenPort}")
  private int port;

  private RemotingServer remotingServer;

  private static final Integer cpus = Runtime.getRuntime().availableProcessors();

  @PostConstruct
  public void init() {
    NettyServerConfig nettyServerConfig = new NettyServerConfig();
    nettyServerConfig.setListenPort(port);
    this.remotingServer = new NettyRemotingServer(nettyServerConfig, channelKeeping);
    this.registerProcessor();
    DtsXID.setIpAddress(NetUtil.getLocalIp());
    DtsXID.setPort(port);
  }

  @PreDestroy
  public void stop() {
    if (this.channelKeeping != null) {
      this.channelKeeping.stop();
    }
    if (this.remotingServer != null) {
      this.remotingServer.shutdown();
    }
  }

  public void start() {
    if (this.remotingServer != null) {
      this.remotingServer.start();
    }
    if (this.channelKeeping != null) {
      this.channelKeeping.start();
    }
    ServerCluster.getServerCluster().registry(port);
  }


  private void registerHeaderRequest() {
    DtsMessageProcessor messageProcessor = createMessageProcessor();
    BlockingQueue<Runnable> clientThreadPoolQueue = Queues.newLinkedBlockingDeque(10000);
    ExecutorService clientMessageExecutor =
        new ServerFixedThreadPoolExecutor(cpus * 3, cpus * 3, 1000 * 60, TimeUnit.MILLISECONDS,
            clientThreadPoolQueue, new ThreadFactoryImpl("ServerHeaderThread_"));
    this.remotingServer.registerProcessor(RequestCode.HEADER_REQUEST, messageProcessor,
        clientMessageExecutor);
  }

  private void registerBodyRequest() {
    DtsMessageProcessor messageProcessor = createMessageProcessor();
    BlockingQueue<Runnable> resourceThreadPoolQueue = Queues.newLinkedBlockingDeque(10000);
    ExecutorService resourceMessageExecutor =
        new ServerFixedThreadPoolExecutor(cpus, cpus, 1000 * 60, TimeUnit.MILLISECONDS,
            resourceThreadPoolQueue, new ThreadFactoryImpl("ServerBodyThread_"));
    this.remotingServer.registerProcessor(RequestCode.BODY_REQUEST, messageProcessor,
        resourceMessageExecutor);
  }

  private void registerHeatBeatRequest() {
    ExecutorService heatBeatProcessorExecutor =
        Executors.newFixedThreadPool(cpus, new ThreadFactoryImpl("ServerHeadBeatThread_"));
    this.remotingServer.registerProcessor(RequestCode.HEART_BEAT, heatBeatProccessor,
        heatBeatProcessorExecutor);
  }

  @Lookup(value = "dtsMessageProcessor")
  protected DtsMessageProcessor createMessageProcessor() {
    return null;
  }

  private void registerProcessor() {
    registerHeaderRequest();
    registerBodyRequest();
    registerHeatBeatRequest();
  }


  public RemotingServer getRemotingServer() {
    return remotingServer;
  }
}
