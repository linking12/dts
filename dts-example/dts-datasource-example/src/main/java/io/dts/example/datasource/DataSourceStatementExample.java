package io.dts.example.datasource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Stopwatch;

import javax.sql.DataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
public class DataSourceStatementExample {

  public static void main(String[] args) {

    NettyClientConfig nettyClientConfig = new NettyClientConfig();
    nettyClientConfig.setConnectTimeoutMillis(30000);
    DtsRemotingClient dtsClient = new DtsRemotingClient(nettyClientConfig, Collections.singletonList("10.9.27.196:10086"));
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
          Stopwatch stopwatch = Stopwatch.createStarted();
          executeStatement(clientMessageSender);
          System.out.println("executePrepareStatement");
          executeUpdatePrepareStatement(clientMessageSender);
          executeInsertPrepareStatement(clientMessageSender);
          stopwatch.stop();
          System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS));
          return 1;
        }
      };

      transactionTemplate.execute(dtsTransactionCallback, 30000l);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      dtsClient.shutdown();
    }
  }

  private static void executeStatement(final DtsClientMessageSenderImpl clientMessageSender) {
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
        return jdbcTemplate.update("update example set value='boddi12345' where id=1");
      }
    });
  }

  private static void executeUpdatePrepareStatement(final DtsClientMessageSenderImpl clientMessageSender)
      throws SQLException {
    final ResourceManager resourceManager = new BaseResourceManager(clientMessageSender);
    resourceManager.setTimeout(30000l);
    DtsDataSource dtsDataSource = new DtsDataSource(dataSource(), "dts");
    dtsDataSource.setResourceManager(resourceManager);
    DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dtsDataSource);
    TransactionTemplate transactionTemplate = new TransactionTemplate(dataSourceTransactionManager);

    JdbcTemplate jdbcTemplate = new JdbcTemplate(dtsDataSource);
    List<String> result = jdbcTemplate.execute("SELECT name,value FROM example where id=?",
        new PreparedStatementCallback<List<String>>() {
          @Override
          public List<String> doInPreparedStatement(final PreparedStatement ps) throws SQLException, DataAccessException {
            ps.setInt(1, 1);
            ResultSet resultSet = ps.executeQuery();
            List<String> names = new ArrayList<>();
            while (resultSet.next()) {
              names.add(resultSet.getString("value"));
            }
            return names;

          }
        });

    result.forEach(e-> System.out.println(e));

    transactionTemplate.execute(new TransactionCallback<Integer>() {
      @Override
      public Integer doInTransaction(final TransactionStatus status) {
        return jdbcTemplate.update("update example set value='boddi222' where id=?", new PreparedStatementSetter() {
          @Override
          public void setValues(final PreparedStatement ps) throws SQLException {
            ps.setInt(1, 1);
          }
        });
      }
    });
  }

  private static void executeInsertPrepareStatement(final DtsClientMessageSenderImpl clientMessageSender)
      throws SQLException {
    final ResourceManager resourceManager = new BaseResourceManager(clientMessageSender);
    resourceManager.setTimeout(30000l);
    DtsDataSource dtsDataSource = new DtsDataSource(dataSource(), "dts");
    dtsDataSource.setResourceManager(resourceManager);
    DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dtsDataSource);
    TransactionTemplate transactionTemplate = new TransactionTemplate(dataSourceTransactionManager);

    JdbcTemplate jdbcTemplate = new JdbcTemplate(dtsDataSource);


    transactionTemplate.execute(new TransactionCallback<Integer>() {
      @Override
      public Integer doInTransaction(final TransactionStatus status) {
        return jdbcTemplate.update("insert into example(name,value) values(?,?)", new PreparedStatementSetter() {
          @Override
          public void setValues(final PreparedStatement ps) throws SQLException {
            ps.setString(1, "testadsf");
            ps.setString(2, "fds");
          }
        });
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
