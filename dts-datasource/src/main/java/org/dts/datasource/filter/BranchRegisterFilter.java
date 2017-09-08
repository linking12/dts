package org.dts.datasource.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by sunjian on 2017/6/16.
 */
public class BranchRegisterFilter extends StatFilter {

  private static final Logger log = LoggerFactory.getLogger(BranchRegisterFilter.class);

  private final List<StatementExecuteListener> statementExecuteListeners;

  public BranchRegisterFilter(List<StatementExecuteListener> statementExecuteListeners) {
    this.statementExecuteListeners = statementExecuteListeners;
  }

  @Override
  public void connection_setAutoCommit(final FilterChain chain, final ConnectionProxy connection,
      final boolean autoCommit)
      throws SQLException {
    super.connection_setAutoCommit(chain, connection, autoCommit);
    if (!autoCommit) {
      if (statementExecuteListeners != null && !statementExecuteListeners.isEmpty()) {
        for (StatementExecuteListener statementExecuteListener : statementExecuteListeners) {
          try {
            statementExecuteListener.afterSetAutoCommit(connection, autoCommit);
          } catch (Exception e) {
            log.error("listener error", e);
          }
        }
      }
    }
  }

  @Override
  public void statementCreateAfter(StatementProxy statement) {
    super.statementCreateAfter(statement);
    statementListenerBefore(statement);
  }

  @Override
  public void statementPrepareAfter(PreparedStatementProxy statement) {
    super.statementPrepareAfter(statement);
    statementListenerBefore(statement);
  }

  private void statementListenerBefore(final StatementProxy statement) {
    if (statementExecuteListeners != null && !statementExecuteListeners.isEmpty()) {
      for (StatementExecuteListener statementExecuteListener : statementExecuteListeners) {
        try {
          statementExecuteListener.afterStatementCreate(statement);
        } catch (Exception e) {
          log.error("listener error", e);
        }
      }
    }
  }

  @Override
  protected void statementExecuteQueryBefore(StatementProxy statement, String sql) {
    super.statementExecuteQueryBefore(statement, sql);
    callListenerBefore(statement, sql);
  }

  @Override
  protected void statementExecuteQueryAfter(StatementProxy statement, String sql,
      ResultSetProxy resultSet) {
    super.statementExecuteQueryAfter(statement, sql, resultSet);
    callListenerAfter(statement, sql, null);
  }

  @Override
  protected void statementExecuteBefore(StatementProxy statement, String sql) {
    super.statementExecuteBefore(statement, sql);
    callListenerBefore(statement, sql);
  }

  @Override
  protected void statementExecuteAfter(StatementProxy statement, String sql, boolean firstResult) {
    super.statementExecuteAfter(statement, sql, firstResult);
    callListenerAfter(statement, sql, null);
  }


  @Override
  protected void statementExecuteBatchBefore(StatementProxy statement) {
    super.statementExecuteBatchBefore(statement);
    callListenerBefore(statement, statement.getBatchSql());
  }

  @Override
  protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {
    super.statementExecuteBatchAfter(statement, result);
    callListenerAfter(statement, statement.getBatchSql(), null);
  }


  @Override
  protected void statementExecuteUpdateBefore(StatementProxy statement, String sql) {
    super.statementExecuteUpdateBefore(statement, sql);
    callListenerBefore(statement, sql);
  }

  @Override
  protected void statementExecuteUpdateAfter(StatementProxy statement, String sql,
      int updateCount) {
    super.statementExecuteUpdateAfter(statement, sql, updateCount);
    callListenerAfter(statement, sql, null);
  }

  @Override
  protected void statement_executeErrorAfter(StatementProxy statement, String sql,
      Throwable error) {
    super.statement_executeErrorAfter(statement, sql, error);
    callListenerAfter(statement, sql, error);
  }

  protected void callListenerBefore(StatementProxy statement, String sql) {
    if (statementExecuteListeners != null && !statementExecuteListeners.isEmpty()) {
      for (StatementExecuteListener statementExecuteListener : statementExecuteListeners) {
        try {
          statementExecuteListener.beforeExecute(statement, sql);
        } catch (Exception e) {
          log.error("listener error", e);
        }
      }
    }
  }

  protected void callListenerAfter(StatementProxy statement, String sql, Throwable error) {
    if (statementExecuteListeners != null && !statementExecuteListeners.isEmpty()) {
      for (StatementExecuteListener statementExecuteListener : statementExecuteListeners) {
        try {
          statementExecuteListener.afterExecute(statement, sql, error);
        } catch (Exception e) {
          log.error("listener error", e);
        }
      }
    }
  }

}
