package io.dts.datasource.wrapper.executor;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import io.dts.common.common.exception.DtsException;
import io.dts.datasource.commiter.AtExecutorRUnCommiter;

/**
 * Created by guoyubo on 2017/9/21.
 */
public abstract class AbstractExecutor {

  public <T> T executeStatement(final StatementModel statementUnit,
      final ExecuteCallback<T> executeCallback) throws Exception {
    return execute(statementUnit, Collections.emptyList(), executeCallback);
  }

  public <T> T executePreparedStatement(final StatementModel preparedStatementUnits,
      final List<Object> parameters, final ExecuteCallback<T> executeCallback)

      throws Exception {
    return execute(preparedStatementUnits, parameters, executeCallback);
  }

  private <T> T execute(StatementModel baseStatementUnit, final List<Object> parameterSet,
      final ExecuteCallback<T> executeCallback) throws Exception {
    T result = executeInternal(baseStatementUnit, parameterSet, executeCallback);
    return result;
  }

  private <T> T executeInternal(final StatementModel baseStatementUnit,
      final List<Object> parameterSet, final ExecuteCallback<T> executeCallback) throws Exception {
    try {
      AtExecutorRUnCommiter commiter = new AtExecutorRUnCommiter(baseStatementUnit, parameterSet);
      commiter.beforeExecute();
      T result = executeCallback.execute(baseStatementUnit);
      commiter.afterExecute();
      return result;
    } catch (final SQLException ex) {
      throw new DtsException(ex);
    }

  }


  static interface ExecuteCallback<T> {

    T execute(StatementModel baseStatementUnit) throws Exception;
  }



}
