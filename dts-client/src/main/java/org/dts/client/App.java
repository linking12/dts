package org.dts.client;


import com.quancheng.dts.common.DtsContext;
import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.rpc.cluster.ZookeeperAddressManager;
import com.quancheng.dts.rpc.remoting.netty.DtsClientImpl;
import com.quancheng.dts.rpc.remoting.netty.NettyClientConfig;

/**
 * Created by guoyubo on 2017/8/30.
 */
public class App {

  public static void main(String[] args) throws DtsException {

    NettyClientConfig nettyClientConfig = new NettyClientConfig();
    nettyClientConfig.setConnectTimeoutMillis(3000);
    DtsClientImpl dtsClient = new DtsClientImpl(nettyClientConfig);
    try {
      dtsClient.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
      dtsClient.setGroup("Default");
      dtsClient.setAppName("Demo");
      dtsClient.start();
      DefaultDtsTransactionManager transactionManager = new DefaultDtsTransactionManager(dtsClient);
      transactionManager.begin(3000L);
      System.out.println(DtsContext.getCurrentXid());
      transactionManager.commit();
    } finally {
      dtsClient.shutdown();

    }


//    final NettyClientConfig nettyClientConfig = new NettyClientConfig();
//    nettyClientConfig.setConnectTimeoutMillis(3000);
//    RemotingClient remotingClient = new NettyRemotingClient(nettyClientConfig);
//    remotingClient.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
//    remotingClient.start();
//
//    final CommandCustomHeader requestHeader = null;
//    RemotingCommand request =
//        RemotingCommand.createRequestCommand(RequestCode.TRANSACTION_BEGIN, requestHeader);
//    RemotingCommand response = remotingClient.invokeSync(NetUtil.getLocalIp() + ":9876", request, 3000);
//    assert response != null;
//    switch (response.getCode()) {
//      case ResponseCode.SUCCESS: {
//        TransactionBeginBody transactionBeginBody = TransactionBeginBody.decode(response.getBody(), TransactionBeginBody.class);
//        System.out.println(transactionBeginBody.getXid());
//      }
//      default:
//        break;
//    }
//    remotingClient.shutdown();
  }
}
