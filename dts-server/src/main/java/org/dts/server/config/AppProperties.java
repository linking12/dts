package org.dts.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.quancheng.dts.rpc.remoting.netty.NettySystemConfig;

import lombok.Data;

/**
 * Created by guoyubo on 2017/9/1.
 */
@Component
@ConfigurationProperties("app")
@Data
public class AppProperties {

  private String name;
  private String group;
  private String zkAddress;

  //netty server config
  private int listenPort = 8888;
  private int serverWorkerThreads = 8;
  private int serverCallbackExecutorThreads = 0;
  private int serverSelectorThreads = 3;
  private int serverOnewaySemaphoreValue = 256;
  private int serverAsyncSemaphoreValue = 64;
  private int serverChannelMaxIdleTimeSeconds = 120;

  private int serverSocketSndBufSize = NettySystemConfig.socketSndbufSize;
  private int serverSocketRcvBufSize = NettySystemConfig.socketRcvbufSize;
}
