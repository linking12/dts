package io.dts.datasource.executor;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Optional;

import io.dts.common.util.event.EventBusInstance;
import io.dts.datasource.executor.commiter.AtExecutorRUnCommiter;
import io.dts.datasource.executor.event.AbstractExecutionEvent;
import io.dts.datasource.executor.event.DMLExecutionEvent;
import io.dts.datasource.executor.event.DQLExecutionEvent;
import io.dts.datasource.executor.event.EventExecutionType;
import io.dts.datasource.executor.prepared.PreparedStatementUnit;
import io.dts.datasource.executor.statement.StatementUnit;
import io.dts.parser.constant.SqlType;

/**
 * Created by guoyubo on 2017/9/21.
 */
public class ExecutorEngine {

  private static ExecutorEngine instance = new ExecutorEngine();

  public static ExecutorEngine getInstance() {
    return instance;
  }

  /**
   * Execute statement.
   *
   * @param statementUnit statement execute unit
   * @param executeCallback statement execute callback
   * @param <T> class type of return value
   * @return execute result
   */
  public <T> T executeStatement(final StatementUnit statementUnit,
      final ExecuteCallback<T> executeCallback) throws Exception {
    return execute(statementUnit, Collections.emptyList(), executeCallback);
  }



  /**
   * Execute prepared statement.
   *
   * @param preparedStatementUnits prepared statement execute unit
   * @param parameters parameters for SQL placeholder
   * @param executeCallback prepared statement execute callback
   * @param <T> class type of return value
   * @return execute result
   */
  public <T> T executePreparedStatement(final PreparedStatementUnit preparedStatementUnits,
      final List<Object> parameters, final ExecuteCallback<T> executeCallback)

      throws Exception {
    return execute(preparedStatementUnits, parameters, executeCallback);
  }


  private <T> T execute(BaseStatementUnit baseStatementUnit, final List<Object> parameterSet,
      final ExecuteCallback<T> executeCallback) throws Exception {
    T result = executeInternal(baseStatementUnit, parameterSet, executeCallback);
    return result;
  }


  private <T> T executeInternal(final BaseStatementUnit baseStatementUnit,
      final List<Object> parameterSet, final ExecuteCallback<T> executeCallback) throws Exception {
    T result;
    List<AbstractExecutionEvent> events = new LinkedList<>();
    events.add(getExecutionEvent(baseStatementUnit, parameterSet));

    for (AbstractExecutionEvent event : events) {
      EventBusInstance.getInstance().post(event);
    }

    try {
      AtExecutorRUnCommiter commiter = new AtExecutorRUnCommiter(baseStatementUnit, parameterSet);
      commiter.beforeExecute();
      result = executeCallback.execute(baseStatementUnit);
      commiter.afterExecute();
    } catch (final SQLException ex) {
      for (AbstractExecutionEvent each : events) {
        each.setEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        each.setException(Optional.of(ex));
        EventBusInstance.getInstance().post(each);
        ExecutorExceptionHandler.handleException(ex);
      }
      return null;
    }

    for (AbstractExecutionEvent each : events) {
      each.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
      EventBusInstance.getInstance().post(each);
    }
    return result;
  }



  private AbstractExecutionEvent getExecutionEvent(final BaseStatementUnit baseStatementUnit,
      final List<Object> parameters)

      throws SQLException {
    AbstractExecutionEvent result;
    if (SqlType.SELECT == baseStatementUnit.getSqlExecutionUnit().getSqlType()) {
      result =
          new DQLExecutionEvent(baseStatementUnit.getSqlExecutionUnit().getDataSource().getDbName(),
              baseStatementUnit.getSqlExecutionUnit().getSql(), parameters);
    } else {
      result =
          new DMLExecutionEvent(baseStatementUnit.getSqlExecutionUnit().getDataSource().getDbName(),
              baseStatementUnit.getSqlExecutionUnit().getSql(), parameters);
    }
    return result;
  }


}
