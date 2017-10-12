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
package io.dts.server.remoting.channel;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.dts.common.component.AbstractLifecycleComponent;
import io.dts.common.util.thread.ThreadFactoryImpl;
import io.dts.remoting.ChannelEventListener;
import io.netty.channel.Channel;

/**
 * @author liushiming
 * @version ChannelHousekeepingService.java, v 0.0.1 2017年9月6日 上午10:16:20 liushiming
 */
@Component
public class ChannelkeepingComponent extends AbstractLifecycleComponent
    implements ChannelEventListener {

  private static final Logger logger = LoggerFactory.getLogger(ChannelkeepingComponent.class);

  @Autowired
  private ChannelRepository channelRepository;

  private final ScheduledExecutorService scheduledExecutorService = Executors
      .newSingleThreadScheduledExecutor(new ThreadFactoryImpl("ClientHousekeepingScheduledThread"));

  @Override
  protected void doStart() {
    this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          channelRepository.scanNotActiveChannel();
        } catch (Exception e) {
          logger.error("", e);
        }
      }
    }, 1000 * 10, 1000 * 10, TimeUnit.MILLISECONDS);
  }

  @Override
  protected void doStop() {
    this.scheduledExecutorService.shutdown();
  }

  @Override
  protected void doClose() throws IOException {}

  @Override
  public void onChannelConnect(String remoteAddr, Channel channel) {}


  @Override
  public void onChannelClose(String remoteAddr, Channel channel) {
    channelRepository.doChannelCloseEvent(remoteAddr, channel);
  }


  @Override
  public void onChannelException(String remoteAddr, Channel channel) {
    channelRepository.doChannelCloseEvent(remoteAddr, channel);
  }


  @Override
  public void onChannelIdle(String remoteAddr, Channel channel) {
    channelRepository.doChannelCloseEvent(remoteAddr, channel);
  }

}
