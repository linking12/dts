package io.dts.parser.visitor;


import org.junit.Before;
import org.junit.Test;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import io.dts.parser.TxcVisitorFactory;
import io.dts.parser.constant.DatabaseType;
import io.dts.parser.vistor.ITxcVisitor;

/**
 * Created by guoyubo on 2017/10/20.
 */
public class TxcUpdateVisitorTest {

  private Connection connection;

  @Before
  public void init() throws SQLException {
    connection = dataSource().getConnection();
  }

  private static DataSource dataSource() {
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/dts");
    dataSource.setUsername("root");
    dataSource.setPassword("123");
    dataSource.setMaxActive(15);
    return dataSource;
  }


  @Test
  public void parse() throws SQLException {
    Statement statement = connection.createStatement();
    String sql = "update txc_global_log l set l.state = 5 where l.tx_id=2";
    ITxcVisitor visitor = TxcVisitorFactory.createSqlVisitor(DatabaseType.MySQL, connection, sql,
        Lists.newArrayList());
    visitor.buildTableMeta();
    visitor.executeAndGetFrontImage(statement);
    System.out.println(visitor.getSelectSql());
    System.out.println(JSON.toJSON(visitor.getTableOriginalValue()));
    statement.execute(sql);
    visitor.executeAndGetRearImage(statement);
    System.out.println(JSON.toJSON(visitor.getTablePresentValue()));
  }


}
