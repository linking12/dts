package org.dts.client;

import org.dts.client.annotation.EnableDtsTransactionManagement;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.rpc.cluster.ZookeeperAddressManager;
import com.quancheng.dts.rpc.remoting.netty.DtsClientImpl;
import com.quancheng.dts.rpc.remoting.netty.NettyClientConfig;

/**
 * Created by guoyubo on 2017/8/30.
 */
@ComponentScan("org.dts.client")
@SpringBootApplication
@Configurable
@EnableDtsTransactionManagement
public class Application {


    @Bean
    public DtsTransactionManager transactionManager() {
      NettyClientConfig nettyClientConfig = new NettyClientConfig();
      nettyClientConfig.setConnectTimeoutMillis(3000);
      DtsClientImpl dtsClient = new DtsClientImpl(nettyClientConfig);
      dtsClient.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
      dtsClient.setGroup("Default");
      dtsClient.setAppName("Demo");
      dtsClient.start();
      DefaultDtsTransactionManager transactionManager = new DefaultDtsTransactionManager(dtsClient);
      return transactionManager;
    }


  public static void main(String[] args) throws DtsException {
    new SpringApplicationBuilder(Application.class).web(true).build(args).run();


//    NettyClientConfig nettyClientConfig = new NettyClientConfig();
//    nettyClientConfig.setConnectTimeoutMillis(3000);
//    DtsClientImpl dtsClient = new DtsClientImpl(nettyClientConfig);
//    try {
//      dtsClient.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
//      dtsClient.setGroup("Default");
//      dtsClient.setAppName("Demo");
//      dtsClient.start();
//      DefaultDtsTransactionManager transactionManager = new DefaultDtsTransactionManager(dtsClient);
//      transactionManager.begin(3000L);
//      System.out.println(DtsContext.getCurrentXid());
//      transactionManager.commit();
//    } finally {
//      dtsClient.shutdown();
//
//    }


  }
}
