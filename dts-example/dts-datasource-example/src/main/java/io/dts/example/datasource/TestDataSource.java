package io.dts.example.datasource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCallback;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import io.dts.client.DefaultDtsTransactionManager;
import io.dts.client.DtsTransactionManager;
import io.dts.datasource.core.DtsDataSource;
import io.dts.remoting.netty.NettyClientConfig;
import io.dts.resourcemanager.core.ResourceManager;
import io.dts.resourcemanager.core.at.BaseResourceManager;
import io.dts.resourcemanager.remoting.DtsRemotingClient;
import io.dts.resourcemanager.remoting.sender.DtsClientMessageSenderImpl;

/**
 * Created by guoyubo on 2017/9/26.
 */
public class TestDataSource {

  public static void main(String[] args) {



    NettyClientConfig nettyClientConfig = new NettyClientConfig();
    nettyClientConfig.setConnectTimeoutMillis(3000);
    DtsRemotingClient dtsClient = new DtsRemotingClient(nettyClientConfig, Collections.singletonList("127.0.0.1:10086"));
//     dtsClient.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
//     dtsClient.setGroup("Default");
//     dtsClient.setAppName("Demo");
    dtsClient.start();
    DtsClientMessageSenderImpl clientMessageSender = new DtsClientMessageSenderImpl(dtsClient);

    DtsTransactionManager  dtsTransactionManager = new DefaultDtsTransactionManager(clientMessageSender);
    dtsTransactionManager.begin(3000l);

    try {
      final ResourceManager resourceManager = new BaseResourceManager(clientMessageSender);
      DtsDataSource dtsDataSource = new DtsDataSource(dataSource(), "dts", resourceManager);
      JdbcTemplate jdbcTemplate = new JdbcTemplate(dtsDataSource);
      List<Long> result = jdbcTemplate.query("SELECT tx_id FROM txc_global_log", new RowMapper<Long>() {
        @Override
        public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
          return rs.getLong(1);
        }
      });
      for (Long id : result) {
        System.out.println(id);
      }

      jdbcTemplate.execute("update txc_global_log set state=2 where tx_id=2");

      dtsTransactionManager.commit();
    } finally {
      dtsClient.shutdown();
    }
  }

//  driverClassName: com.mysql.jdbc.Driver
//  url: jdbc:mysql://127.0.0.1:3306/dts
//  username: root
//  password: 123456
//  maxActive: 15
//  minIdle: 5
  private static DataSource dataSource() {
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/dts");
    dataSource.setUsername("root");
    dataSource.setPassword("123456");
    dataSource.setMaxActive(15);
    return dataSource;
  }

}
