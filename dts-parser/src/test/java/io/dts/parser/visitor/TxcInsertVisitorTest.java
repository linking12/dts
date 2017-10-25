package io.dts.parser.visitor;


import org.junit.Before;
import org.junit.Test;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import io.dts.parser.TxcVisitorFactory;
import io.dts.parser.constant.DatabaseType;
import io.dts.parser.vistor.ITxcVisitor;
import sun.swing.BakedArrayList;

/**
 * Created by guoyubo on 2017/10/20.
 */
public class TxcInsertVisitorTest {

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
    List<Object> parameters = Lists.newArrayList(1, 2);
    String sql =
        "insert into txc_global_log (state,mid,gmt_created,gmt_modified) values (?,?,now(),now())";
    PreparedStatement statement = connection.prepareStatement(sql);

    ITxcVisitor visitor =
        TxcVisitorFactory.createSqlVisitor(DatabaseType.MySQL, connection, sql, parameters);
    visitor.buildTableMeta();
    visitor.executeAndGetFrontImage(statement);
    System.out.println(visitor.getSelectSql());
    System.out.println(visitor.getTableOriginalValue());
    statement.setInt(1, 1);
    statement.setInt(2, 2);
    statement.execute();
    visitor.executeAndGetRearImage(statement);
    System.out.println(visitor.getTablePresentValue());
  }


}
