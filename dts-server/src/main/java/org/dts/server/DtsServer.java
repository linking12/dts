package org.dts.server;

import org.dts.server.processor.DefaultRequestProcessor;

import com.quancheng.dts.rpc.cluster.ZookeeperAddressManager;
import com.quancheng.dts.rpc.remoting.RemotingServer;
import com.quancheng.dts.rpc.remoting.netty.NettyRemotingServer;
import com.quancheng.dts.rpc.remoting.netty.NettyServerConfig;
import com.quancheng.dts.util.ThreadFactoryImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guoyubo on 2017/8/30.
 */
public class DtsServer {

  public static final String DTS_REGISTER_ROOT_PATH = "/dts";

  private RemotingServer remotingServer;

  private String group;

  private int port;

  public DtsServer(final String group, final int port, final String clusterAddress) {
    this.group = group;
    this.port = port;
    final NettyServerConfig nettyServerConfig = new NettyServerConfig();
    nettyServerConfig.setListenPort(port);
    remotingServer = new NettyRemotingServer(nettyServerConfig);
    remotingServer.setAddressManager(new ZookeeperAddressManager(clusterAddress, DTS_REGISTER_ROOT_PATH));
    ExecutorService remotingExecutor =
        Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(),
            new ThreadFactoryImpl("RemotingExecutorThread_"));
    remotingServer.registerDefaultProcessor(new DefaultRequestProcessor(), remotingExecutor);
  }

  public void start() {
    remotingServer.start();
  }

  public void shutdown() {
    remotingServer.shutdown();
  }

  public String getGroup() {
    return group;
  }

  public int getPort() {
    return port;
  }
}
