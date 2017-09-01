package org.dts.server;

import org.dts.server.processor.DefaultRequestProcessor;

import com.quancheng.dts.rpc.cluster.ZookeeperAddressManager;
import com.quancheng.dts.rpc.remoting.RemotingServer;
import com.quancheng.dts.rpc.remoting.netty.NettyRemotingServer;
import com.quancheng.dts.rpc.remoting.netty.NettyServerConfig;
import com.quancheng.dts.util.ThreadFactoryImpl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guoyubo on 2017/8/30.
 */
public class DtsServer {

  public static final String DTS_REGISTER_ROOT_PATH = "/dts";

  private RemotingServer remotingServer;

  private String group;

  public DtsServer(final String group, final String zkAddress, NettyServerConfig nettyServerConfig) {
    this.group = group;
    if (nettyServerConfig == null) {
      nettyServerConfig = new NettyServerConfig();
    }
    remotingServer = new NettyRemotingServer(nettyServerConfig);
    remotingServer.setGroup(group);
    remotingServer.setAddressManager(new ZookeeperAddressManager(zkAddress, DTS_REGISTER_ROOT_PATH));
    ExecutorService remotingExecutor =
        Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(),
            new ThreadFactoryImpl("RemotingExecutorThread_"));
    remotingServer.registerDefaultProcessor(new DefaultRequestProcessor(), remotingExecutor);
  }

  @PostConstruct
  public void start() {
    remotingServer.start();
  }

  @PreDestroy
  public void shutdown() {
    remotingServer.shutdown();
  }

  public String getGroup() {
    return group;
  }

  public static void main( String[] args )
  {
    final NettyServerConfig nettyServerConfig = new NettyServerConfig();
    nettyServerConfig.setListenPort(9876);
    DtsServer dtsServer = new DtsServer("Default", "localhost:2181", nettyServerConfig);
    dtsServer.start();
  }
}
