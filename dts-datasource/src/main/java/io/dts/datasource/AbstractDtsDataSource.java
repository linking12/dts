package io.dts.datasource;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import io.dts.resourcemanager.api.IDtsDataSource;


/**
 * Created by guoyubo on 2017/9/20.
 */
public abstract class AbstractDtsDataSource implements IDtsDataSource {

  @Override
  public <T> T unwrap(final Class<T> iface) throws SQLException {
    return getRawDataSource().unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(final Class<?> iface) throws SQLException {
    return getRawDataSource().isWrapperFor(iface);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return getRawDataSource().getLogWriter();
  }

  @Override
  public void setLogWriter(final PrintWriter out) throws SQLException {
    getRawDataSource().setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(final int seconds) throws SQLException {
    getRawDataSource().setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return getRawDataSource().getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException();
  }
}
