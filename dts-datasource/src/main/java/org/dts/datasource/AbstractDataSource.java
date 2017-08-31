package org.dts.datasource;

import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Created by guoyubo on 2017/8/22.
 */
public abstract class AbstractDataSource implements DataSource {

  protected PrintWriter logWriter;


  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return iface != null && iface.isInstance(this);

  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T unwrap(Class<T> iface) {
    if (iface == null) {
      return null;
    }

    if (iface.isInstance(this)) {
      return (T) this;
    }

    return null;
  }

  @Override
  public PrintWriter getLogWriter() {
    return logWriter;
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    this.logWriter = out;
  }


  @Override
  public void setLoginTimeout(int seconds) {
    DriverManager.setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() {
    return DriverManager.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException();
  }
}
