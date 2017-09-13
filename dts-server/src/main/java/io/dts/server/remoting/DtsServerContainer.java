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

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.collect.Queues;

import io.dts.common.ThreadFactoryImpl;
import io.dts.common.common.TxcXID;
import io.dts.common.protocol.RequestCode;
import io.dts.remoting.RemotingServer;
import io.dts.remoting.netty.NettyRemotingServer;
import io.dts.remoting.netty.NettyServerConfig;
import io.dts.server.DtsServerProperties;
import io.dts.server.common.AbstractLifecycleComponent;
import io.dts.server.remoting.channel.ChannelkeepingComponent;
import io.dts.server.remoting.latency.ServerFixedThreadPoolExecutor;
import io.dts.server.remoting.processor.BizMessageProcessor;
import io.dts.server.remoting.processor.HeatBeatProcessor;
import io.dts.util.NetUtil;

/**
 * @author liushiming
 * @version DtsServerControllerComponent.java, v 0.0.1 2017年9月13日 下午1:58:27 liushiming
 */
@Component
public class DtsServerContainer extends AbstractLifecycleComponent {

  @Autowired
  private ChannelkeepingComponent channelKeeping;

  @Autowired
  private DtsServerProperties serverProperties;

  @Autowired
  @Qualifier("bizMessageProcessor")
  private BizMessageProcessor bizMessageProccessor;

  @Autowired
  @Qualifier("heatBeatProcessor")
  private HeatBeatProcessor heatBeatProccessor;


  private RemotingServer remotingServer;

  @PostConstruct
  public void init() {
    NettyServerConfig nettyServerConfig = new NettyServerConfig();
    nettyServerConfig.setListenPort(serverProperties.getListenPort());
    this.remotingServer = new NettyRemotingServer(nettyServerConfig, channelKeeping);
    this.registerProcessor();
    TxcXID.setIpAddress(NetUtil.getLocalIp());
    TxcXID.setPort(serverProperties.getListenPort());
  }

  private void registerHeaderRequest() {
    BlockingQueue<Runnable> clientThreadPoolQueue =
        Queues.newLinkedBlockingDeque(serverProperties.getClientThreadPoolQueueSize());
    ExecutorService clientMessageExecutor =
        new ServerFixedThreadPoolExecutor(serverProperties.getClientThreadPoolSize(),
            serverProperties.getClientThreadPoolSize(), 1000 * 60, TimeUnit.MILLISECONDS,
            clientThreadPoolQueue, new ThreadFactoryImpl("ClientMessageThread_"));
    this.remotingServer.registerProcessor(RequestCode.HEADER_REQUEST, bizMessageProccessor,
        clientMessageExecutor);
  }

  private void registerBodyRequest() {
    BlockingQueue<Runnable> resourceThreadPoolQueue =
        Queues.newLinkedBlockingDeque(serverProperties.getResourceThreadPoolQueueSize());
    ExecutorService resourceMessageExecutor =
        new ServerFixedThreadPoolExecutor(serverProperties.getResourceThreadPoolSize(),
            serverProperties.getResourceThreadPoolSize(), 1000 * 60, TimeUnit.MILLISECONDS,
            resourceThreadPoolQueue, new ThreadFactoryImpl("ResourceMessageThread_"));
    this.remotingServer.registerProcessor(RequestCode.BODY_REQUEST, bizMessageProccessor,
        resourceMessageExecutor);
  }

  private void registerHeatBeatRequest() {
    ExecutorService heatBeatProcessorExecutor =
        Executors.newFixedThreadPool(serverProperties.getChannelHeatThreadPoolSize(),
            new ThreadFactoryImpl("ClientManageThread_"));
    this.remotingServer.registerProcessor(RequestCode.HEART_BEAT, heatBeatProccessor,
        heatBeatProcessorExecutor);
  }

  private void registerProcessor() {
    registerHeaderRequest();
    registerBodyRequest();
    registerHeatBeatRequest();
  }

  @Override
  protected void doStart() {
    if (this.remotingServer != null) {
      this.remotingServer.start();
    }
    if (this.channelKeeping != null) {
      this.channelKeeping.start();
    }
  }

  @Override
  protected void doStop() {
    if (this.channelKeeping != null) {
      this.channelKeeping.stop();
    }
    if (this.remotingServer != null) {
      this.remotingServer.shutdown();
    }
  }

  @Override
  protected void doClose() throws IOException {

  }

}
