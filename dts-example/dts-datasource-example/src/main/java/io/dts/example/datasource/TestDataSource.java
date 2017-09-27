package io.dts.example.datasource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import io.dts.client.DefaultDtsTransactionManager;
import io.dts.client.DtsTransactionManager;
import io.dts.client.support.DtsATTransactionTemplate;
import io.dts.client.support.DtsTransactionCallback;
import io.dts.datasource.core.DtsDataSource;
import io.dts.remoting.netty.NettyClientConfig;
import io.dts.resourcemanager.core.ResourceManager;
import io.dts.resourcemanager.core.impl.BaseResourceManager;
import io.dts.resourcemanager.remoting.DtsRemotingClient;
import io.dts.resourcemanager.remoting.sender.DtsClientMessageSenderImpl;

/**
 * Created by guoyubo on 2017/9/26.
 */
public class TestDataSource {

  public static void main(String[] args) {

    NettyClientConfig nettyClientConfig = new NettyClientConfig();
    nettyClientConfig.setConnectTimeoutMillis(30000);
    DtsRemotingClient dtsClient = new DtsRemotingClient(nettyClientConfig, Collections.singletonList("127.0.0.1:10086"));
//     dtsClient.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
//     dtsClient.setGroup("Default");
//     dtsClient.setAppName("Demo");
    dtsClient.start();
    DtsClientMessageSenderImpl clientMessageSender = new DtsClientMessageSenderImpl(dtsClient);

    DtsTransactionManager  dtsTransactionManager = new DefaultDtsTransactionManager(clientMessageSender);

    try {

      DtsATTransactionTemplate transactionTemplate = new DtsATTransactionTemplate(dtsTransactionManager);

      DtsTransactionCallback<Object> dtsTransactionCallback = new DtsTransactionCallback<Object>() {
        @Override
        public Object doInTransaction() throws Throwable {
          branch1Execute(clientMessageSender);
          return null;
        }
      };
      transactionTemplate.execute(dtsTransactionCallback, 30000l);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      dtsClient.shutdown();
    }
  }

  private static void branch1Execute(final DtsClientMessageSenderImpl clientMessageSender) {
    final ResourceManager resourceManager = new BaseResourceManager(clientMessageSender);
    resourceManager.setTimeout(30000l);
    DtsDataSource dtsDataSource = new DtsDataSource(dataSource(), "dts");
    dtsDataSource.setResourceManager(resourceManager);
    DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dtsDataSource);
    TransactionTemplate transactionTemplate = new TransactionTemplate(dataSourceTransactionManager);

    JdbcTemplate jdbcTemplate = new JdbcTemplate(dtsDataSource);
    List<String> result = jdbcTemplate.query("SELECT name,value FROM example", new RowMapper<String>() {
      @Override
      public String mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return rs.getString(1);
      }
    });
    for (String name : result) {
      System.out.println(name);
    }
    transactionTemplate.execute(new TransactionCallback<Integer>() {
      @Override
      public Integer doInTransaction(final TransactionStatus status) {
        return jdbcTemplate.update("update example set value='boddi' where id=1");
      }
    });
  }

  private static DataSource dataSource() {
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/canal_test");
    dataSource.setUsername("root");
    dataSource.setPassword("123456");
    dataSource.setMaxActive(15);
    return dataSource;
  }

}
