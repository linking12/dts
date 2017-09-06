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

  private int writeThreadPoolSize = 16 + Runtime.getRuntime().availableProcessors();

  private int writeThreadPoolQueueSize = 10000;

  private int queryThreadPoolSize = Runtime.getRuntime().availableProcessors() * 2;

  private int queryThreadPoolQueueSize = 10000;

  private int channelHeatThreadPoolSize = 8;

  public int getWriteThreadPoolSize() {
    return writeThreadPoolSize;
  }

  public void setWriteThreadPoolSize(int writeThreadPoolSize) {
    this.writeThreadPoolSize = writeThreadPoolSize;
  }

  public int getWriteThreadPoolQueueSize() {
    return writeThreadPoolQueueSize;
  }

  public void setWriteThreadPoolQueueSize(int writeThreadPoolQueueSize) {
    this.writeThreadPoolQueueSize = writeThreadPoolQueueSize;
  }

  public int getQueryThreadPoolSize() {
    return queryThreadPoolSize;
  }

  public void setQueryThreadPoolSize(int queryThreadPoolSize) {
    this.queryThreadPoolSize = queryThreadPoolSize;
  }

  public int getQueryThreadPoolQueueSize() {
    return queryThreadPoolQueueSize;
  }

  public void setQueryThreadPoolQueueSize(int queryThreadPoolQueueSize) {
    this.queryThreadPoolQueueSize = queryThreadPoolQueueSize;
  }

  public int getChannelHeatThreadPoolSize() {
    return channelHeatThreadPoolSize;
  }

  public void setChannelHeatThreadPoolSize(int channelHeatThreadPoolSize) {
    this.channelHeatThreadPoolSize = channelHeatThreadPoolSize;
  }

  public int getListenPort() {
    return listenPort;
  }

  public void setListenPort(int listenPort) {
    this.listenPort = listenPort;
  }

}
