package org.dts.server;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

//        final NettyServerConfig nettyServerConfig = new NettyServerConfig();
//        nettyServerConfig.setListenPort(9876);
//        RemotingServer remotingServer = new NettyRemotingServer(nettyServerConfig);
//        remotingServer.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
//        ExecutorService remotingExecutor =
//            Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(), new ThreadFactoryImpl("RemotingExecutorThread_"));
//        remotingServer.registerDefaultProcessor(new DefaultRequestProcessor(), remotingExecutor);
//        remotingServer.start();

        DtsServer dtsServer = new DtsServer("DEFAULT", 9876, "localhost:2181");
        dtsServer.start();
    }
}
