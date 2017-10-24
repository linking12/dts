package io.dts.datasource.wrapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.dts.common.common.context.DtsContext;
import io.dts.datasource.DtsConnection;
import io.dts.datasource.wrapper.executor.PreparedStatementExecutor;

/**
 * Created by guoyubo on 2017/9/26.
 */
public class DtsPrepareStatementWrapper extends AbstractDtsPrepareStatement {


  public DtsPrepareStatementWrapper(final DtsConnection dtsConnection,
      final PreparedStatement statement, String sql) {
    super(dtsConnection, statement);
    setTargetSql(sql);
  }

  @Override
  public ResultSet executeQuery() throws SQLException {
    try {
      return new PreparedStatementExecutor(createStatementModel(getTargetSql()), getParameters())
          .executeQuery();
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }


  @Override
  public int executeUpdate() throws SQLException {
    try {
      return new PreparedStatementExecutor(createStatementModel(getTargetSql()), getParameters())
          .executeUpdate();
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }

  @Override
  public boolean execute() throws SQLException {
    try {
      return new PreparedStatementExecutor(createStatementModel(getTargetSql()), getParameters())
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
