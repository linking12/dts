package io.dts.datasource;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import io.dts.resourcemanager.api.IDtsConnection;

/**
 * Created by guoyubo on 2017/9/20.
 */
public abstract class AbstractDtsConnection implements IDtsConnection {

  @Override
  public void rollback(final Savepoint savepoint) throws SQLException {
    getRawConnection().rollback(savepoint);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return getRawConnection().isWrapperFor(iface);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return getRawConnection().unwrap(iface);
  }

  @Override
  public void clearWarnings() throws SQLException {
    getRawConnection().clearWarnings();
  }


  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return getRawConnection().createArrayOf(typeName, elements);
  }

  @Override
  public Blob createBlob() throws SQLException {
    return getRawConnection().createBlob();
  }

  @Override
  public Clob createClob() throws SQLException {
    return getRawConnection().createClob();
  }

  @Override
  public NClob createNClob() throws SQLException {
    return getRawConnection().createNClob();
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    return getRawConnection().createSQLXML();
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return getRawConnection().createStruct(typeName, attributes);
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return getRawConnection().getAutoCommit();
  }

  @Override
  public String getCatalog() throws SQLException {
    return getRawConnection().getCatalog();
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    return getRawConnection().getClientInfo();
  }

  @Override
  public String getClientInfo(String name) throws SQLException {
    return getRawConnection().getClientInfo(name);
  }

  @Override
  public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
    try {
      getRawConnection().setClientInfo(name, value);
    } catch (SQLException e) {
      throw new SQLClientInfoException();
    }
  }

  @Override
  public int getHoldability() throws SQLException {
    return getRawConnection().getHoldability();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return getRawConnection().getMetaData();
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return getRawConnection().getTransactionIsolation();
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return getRawConnection().getTypeMap();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return getRawConnection().getWarnings();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return getRawConnection().isClosed();
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return getRawConnection().isReadOnly();
  }

  @Override
  public boolean isValid(int timeout) throws SQLException {
    return getRawConnection().isValid(timeout);
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    return getRawConnection().nativeSQL(sql);
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    getRawConnection().releaseSavepoint(savepoint);
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    getRawConnection().setCatalog(catalog);
  }

  @Override
  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    try {
      getRawConnection().setClientInfo(properties);
    } catch (SQLException e) {
      throw new SQLClientInfoException();
    }
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    getRawConnection().setHoldability(holdability);
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    getRawConnection().setReadOnly(readOnly);
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    return getRawConnection().setSavepoint();
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    return getRawConnection().setSavepoint(name);
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    getRawConnection().setTransactionIsolation(level);
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    getRawConnection().setTypeMap(map);
  }


  public void setSchema(String schema) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public String getSchema() throws SQLException {
    throw new UnsupportedOperationException();
  }

  public void abort(Executor executor) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public int getNetworkTimeout() throws SQLException {
    throw new UnsupportedOperationException();
  }
}
