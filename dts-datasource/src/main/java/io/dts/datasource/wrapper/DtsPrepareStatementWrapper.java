package io.dts.datasource.wrapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.dts.common.common.context.DtsContext;
import io.dts.datasource.DtsConnection;
import io.dts.datasource.wrapper.executor.PreparedStatementExecutor;
import io.dts.datasource.wrapper.executor.StatementUnit;
import io.dts.datasource.wrapper.executor.StatementUnit.SQLExecutionUnit;
import io.dts.resourcemanager.api.IDtsConnection;

/**
 * Created by guoyubo on 2017/9/26.
 */
public class DtsPrepareStatementWrapper extends AbstractDtsPrepareStatement {

  private DtsConnection dtsConnection;

  private PreparedStatement statement;

  public DtsPrepareStatementWrapper(final DtsConnection dtsConnection,
      final PreparedStatement statement, String sql) {
    this.dtsConnection = dtsConnection;
    this.statement = statement;
    setTargetSql(sql);
  }

  @Override
  public IDtsConnection getDtsConnection() throws SQLException {
    return dtsConnection;
  }

  @Override
  public PreparedStatement getRawStatement() throws SQLException {
    return statement;
  }


  @Override
  public ResultSet executeQuery() throws SQLException {
    try {
      return new PreparedStatementExecutor(getStatementUnit(getTargetSql()), getParameters())
          .executeQuery();
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }


  private StatementUnit getStatementUnit(final String sql) throws SQLException {
    final StatementUnit.SQLExecutionUnit sqlExecutionUnit =
        new SQLExecutionUnit(dtsConnection.getDataSource(), sql);
    return new StatementUnit(sqlExecutionUnit, this);
  }


  @Override
  public int executeUpdate() throws SQLException {
    try {
      return new PreparedStatementExecutor(getStatementUnit(getTargetSql()), getParameters())
          .executeUpdate();
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }

  @Override
  public boolean execute() throws SQLException {
    try {
      return new PreparedStatementExecutor(getStatementUnit(getTargetSql()), getParameters())
          .execute();
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }

  @Override
  public void addBatch() throws SQLException {
    if (DtsContext.inTxcTransaction()) {
      throw new UnsupportedOperationException("unsupport add batch in dts transaction");
    }
    getRawStatement().addBatch();
  }

  @Override
  public void addBatch(final String sql) throws SQLException {
    if (DtsContext.inTxcTransaction()) {
      throw new UnsupportedOperationException("unsupport add batch in dts transaction");
    }
    getRawStatement().addBatch(sql);
  }

  @Override
  public void clearBatch() throws SQLException {
    if (DtsContext.inTxcTransaction()) {
      throw new UnsupportedOperationException("unsupport clear batch in dts transaction");
    }
    getRawStatement().clearBatch();
  }

  @Override
  public int[] executeBatch() throws SQLException {
    if (DtsContext.inTxcTransaction()) {
      throw new UnsupportedOperationException("unsupport execute batch in dts transaction");
    }
    return getRawStatement().executeBatch();
  }
}
