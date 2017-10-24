
package io.dts.datasource.wrapper.executor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;


public final class PreparedStatementExecutor extends AbstractExecutor {

  private final StatementUnit preparedStatementUnit;

  private final List<Object> parameters;

  public PreparedStatementExecutor(StatementUnit preparedStatementUnit, List<Object> parameters) {
    super();
    this.preparedStatementUnit = preparedStatementUnit;
    this.parameters = parameters;
  }

  public ResultSet executeQuery() throws Exception {
    return super.executePreparedStatement(preparedStatementUnit, parameters,
        new ExecuteCallback<ResultSet>() {

          @Override
          public ResultSet execute(final StatementUnit baseStatementUnit) throws Exception {
            return ((PreparedStatement) baseStatementUnit.getStatement().getRawStatement())
                .executeQuery();
          }
        });
  }

  public int executeUpdate() throws Exception {
    Integer results = super.executePreparedStatement(preparedStatementUnit, parameters,
        new ExecuteCallback<Integer>() {

          @Override
          public Integer execute(final StatementUnit baseStatementUnit) throws Exception {
            return ((PreparedStatement) baseStatementUnit.getStatement().getRawStatement())
                .executeUpdate();
          }
        });
    return results;
  }


  public boolean execute() throws Exception {
    Boolean result = super.executePreparedStatement(preparedStatementUnit, parameters,
        new ExecuteCallback<Boolean>() {

          @Override
          public Boolean execute(final StatementUnit baseStatementUnit) throws Exception {
            return ((PreparedStatement) baseStatementUnit.getStatement().getRawStatement())
                .execute();
          }
        });
    if (null == result) {
      return false;
    }
    return result;
  }
}
