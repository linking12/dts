package org.dts.datasource.filter;

import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;

import java.sql.SQLException;

/**
 * Created by sunjian on 2017/6/26.
 */
public interface StatementExecuteListener {

  void beforeExecute(StatementProxy statement, String sql) throws SQLException;

  void afterExecute(StatementProxy statement, String sql, Throwable error) throws SQLException;

  void afterStatementCreate(StatementProxy statement) throws SQLException;

  void afterSetAutoCommit(ConnectionProxy connection, final boolean autoCommit);
}
