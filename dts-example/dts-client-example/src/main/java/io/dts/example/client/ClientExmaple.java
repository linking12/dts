package io.dts.example.client;

import java.util.Collections;

import io.dts.client.DefaultDtsTransactionManager;
import io.dts.client.exception.DtsTransactionException;
import io.dts.client.remoting.DtsRemotingClient;
import io.dts.client.remoting.sender.DtsClientMessageSenderImpl;
import io.dts.common.context.DtsContext;
import io.dts.remoting.netty.NettyClientConfig;

/**
 * Created by guoyubo on 2017/9/26.
 */
public class ClientExmaple {


  public static void main(String[] args) {
    NettyClientConfig nettyClientConfig = new NettyClientConfig();
    nettyClientConfig.setConnectTimeoutMillis(3000);
    DtsRemotingClient dtsClient = new DtsRemotingClient(nettyClientConfig, Collections.singletonList("127.0.0.1:10086"));
//     dtsClient.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
//     dtsClient.setGroup("Default");
//     dtsClient.setAppName("Demo");
    dtsClient.start();
    DtsClientMessageSenderImpl clientMessageSender = new DtsClientMessageSenderImpl(dtsClient);
    try {
      DefaultDtsTransactionManager transactionManager =
          new DefaultDtsTransactionManager(clientMessageSender);
      transactionManager.begin(3000L);
      System.out.println(DtsContext.getCurrentXid());
    } catch (DtsTransactionException e) {
      e.printStackTrace();
    } finally {
      dtsClient.shutdown();
    }
  }
}
