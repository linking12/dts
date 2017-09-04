package org.dts.datasource.filter;

import com.alibaba.druid.proxy.jdbc.StatementProxy;

/**
 * Created by sunjian on 2017/6/26.
 */
public interface StatementExecuteListener {

  void beforeExecute(StatementProxy statement, String sql);

  void afterExecute(StatementProxy statement, String sql, Throwable error);

  void afterStatementCreate(StatementProxy statement);
}
