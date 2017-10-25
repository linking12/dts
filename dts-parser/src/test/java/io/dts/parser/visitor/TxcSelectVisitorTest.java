package io.dts.parser.visitor;


import org.junit.Before;
import org.junit.Test;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import io.dts.parser.DtsVisitorFactory;
import io.dts.parser.struct.DatabaseType;
import io.dts.parser.vistor.ITxcVisitor;

/**
 * Created by guoyubo on 2017/10/20.
 */
public class TxcSelectVisitorTest {

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
    dataSource.setPassword("123456");
    dataSource.setMaxActive(15);
    return dataSource;
  }


  @Test
  public void parse() throws SQLException {
    List<Object> parameters = Lists.newArrayList(1, 2);
    String sql = "select l1.tx_id, l1.state from txc_global_log l1 where l1.state=? ";
    PreparedStatement statement = connection.prepareStatement(sql);

    ITxcVisitor visitor = DtsVisitorFactory.createSqlVisitor(
        DatabaseType.MySQL,
        connection,
        sql,
        parameters);
    visitor.buildTableMeta();
    visitor.executeAndGetFrontImage(statement);
    System.out.println(visitor.getSelectSql());
    System.out.println(visitor.getTableOriginalValue());
    statement.setInt(1, 1);
    statement.execute();
    visitor.executeAndGetRearImage(statement);
    System.out.println(visitor.getTablePresentValue());
  }


}
