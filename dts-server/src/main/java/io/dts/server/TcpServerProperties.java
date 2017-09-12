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
package io.dts.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liushiming
 * @version TcpServerProperties.java, v 0.0.1 2017年9月6日 上午11:17:24 liushiming
 */
@ConfigurationProperties(prefix = "tcp")
public class TcpServerProperties {

  private int listenPort;

  private int clientThreadPoolSize = 16 + Runtime.getRuntime().availableProcessors();

  private int clientThreadPoolQueueSize = 10000;

  private int resourceThreadPoolSize = Runtime.getRuntime().availableProcessors() * 2;

  private int resourceThreadPoolQueueSize = 10000;

  private int channelHeatThreadPoolSize = 8;

  public int getListenPort() {
    return listenPort;
  }

  public void setListenPort(int listenPort) {
    this.listenPort = listenPort;
  }

  public int getClientThreadPoolSize() {
    return clientThreadPoolSize;
  }

  public void setClientThreadPoolSize(int clientThreadPoolSize) {
    this.clientThreadPoolSize = clientThreadPoolSize;
  }

  public int getClientThreadPoolQueueSize() {
    return clientThreadPoolQueueSize;
  }

  public void setClientThreadPoolQueueSize(int clientThreadPoolQueueSize) {
    this.clientThreadPoolQueueSize = clientThreadPoolQueueSize;
  }

  public int getResourceThreadPoolSize() {
    return resourceThreadPoolSize;
  }

  public void setResourceThreadPoolSize(int resourceThreadPoolSize) {
    this.resourceThreadPoolSize = resourceThreadPoolSize;
  }

  public int getResourceThreadPoolQueueSize() {
    return resourceThreadPoolQueueSize;
  }

  public void setResourceThreadPoolQueueSize(int resourceThreadPoolQueueSize) {
    this.resourceThreadPoolQueueSize = resourceThreadPoolQueueSize;
  }

  public int getChannelHeatThreadPoolSize() {
    return channelHeatThreadPoolSize;
  }

  public void setChannelHeatThreadPoolSize(int channelHeatThreadPoolSize) {
    this.channelHeatThreadPoolSize = channelHeatThreadPoolSize;
  }



}
