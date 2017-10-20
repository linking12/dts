package io.dts.datasource.wrapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

import io.dts.resourcemanager.api.IDtsStatement;

/**
 * Created by guoyubo on 2017/9/20.
 */
public abstract class AbstractDtsStatement implements IDtsStatement {

  /**
   * 目标SQL
   */
  protected String targetSql;

  @Override
  public String getTargetSql() {
    return targetSql;
  }

  public void setTargetSql(final String targetSql) {
    this.targetSql = targetSql;
  }


  @Override
  public void cancel() throws SQLException {
    getRawStatement().cancel();
  }

  @Override
  public void clearWarnings() throws SQLException {
    getRawStatement().clearWarnings();
  }

  @Override
  public void close() throws SQLException {
    getRawStatement().close();
  }

  @Override
  public Connection getConnection() throws SQLException {
    return getRawStatement().getConnection();
  }

  @Override
  public int getFetchDirection() throws SQLException {
    return getRawStatement().getFetchDirection();
  }

  @Override
  public int getFetchSize() throws SQLException {
    return getRawStatement().getFetchSize();
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException {
    return getRawStatement().getGeneratedKeys();
  }

  @Override
  public int getMaxFieldSize() throws SQLException {
    return getRawStatement().getMaxFieldSize();
  }

  @Override
  public int getMaxRows() throws SQLException {
    return getRawStatement().getMaxRows();
  }

  @Override
  public boolean getMoreResults() throws SQLException {
    return getRawStatement().getMoreResults();
  }

  @Override
  public boolean getMoreResults(int arg0) throws SQLException {
    return getRawStatement().getMoreResults(arg0);
  }

  @Override
  public int getQueryTimeout() throws SQLException {
    return getRawStatement().getQueryTimeout();
  }

  @Override
  public ResultSet getResultSet() throws SQLException {
    return getRawStatement().getResultSet();
  }

  @Override
  public int getResultSetConcurrency() throws SQLException {
    return getRawStatement().getResultSetConcurrency();
  }

  @Override
  public int getResultSetHoldability() throws SQLException {
    return getRawStatement().getResultSetHoldability();
  }

  @Override
  public int getResultSetType() throws SQLException {
    return getRawStatement().getResultSetType();
  }

  @Override
  public int getUpdateCount() throws SQLException {
    return getRawStatement().getUpdateCount();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return getRawStatement().getWarnings();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return getRawStatement().isClosed();
  }

  @Override
  public boolean isPoolable() throws SQLException {
    return getRawStatement().isPoolable();
  }

  @Override
  public void setCursorName(String arg0) throws SQLException {
    getRawStatement().setCursorName(arg0);
  }

  @Override
  public void setEscapeProcessing(boolean arg0) throws SQLException {
    getRawStatement().setEscapeProcessing(arg0);
  }

  @Override
  public void setFetchDirection(int arg0) throws SQLException {
    getRawStatement().setFetchDirection(arg0);
  }

  @Override
  public void setFetchSize(int arg0) throws SQLException {
    getRawStatement().setFetchSize(arg0);
  }

  @Override
  public void setMaxFieldSize(int arg0) throws SQLException {
    getRawStatement().setMaxFieldSize(arg0);
  }

  @Override
  public void setMaxRows(int arg0) throws SQLException {
    getRawStatement().setMaxRows(arg0);
  }

  @Override
  public void setPoolable(boolean arg0) throws SQLException {
    getRawStatement().setPoolable(arg0);
  }

  @Override
  public void setQueryTimeout(int arg0) throws SQLException {
    getRawStatement().setQueryTimeout(arg0);
  }

  @Override
  public boolean isWrapperFor(Class<?> arg0) throws SQLException {
    return getRawStatement().isWrapperFor(arg0);
  }

  @Override
  public <T> T unwrap(Class<T> arg0) throws SQLException {
    return getRawStatement().unwrap(arg0);
  }

  public void closeOnCompletion() throws SQLException {
    throw new UnsupportedOperationException();
  }

  public boolean isCloseOnCompletion() throws SQLException {
    throw new UnsupportedOperationException();
  }


}
