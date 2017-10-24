package io.dts.datasource.wrapper;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.apache.curator.shaded.com.google.common.collect.Lists;

import io.dts.datasource.DtsConnection;
import io.dts.resourcemanager.api.IDtsPrepareStatement;

/**
 * Created by guoyubo on 2017/9/20.
 */
public abstract class AbstractDtsPrepareStatement extends AbstractDtsStatement
    implements IDtsPrepareStatement {

  private final List<Object> parameters;

  AbstractDtsPrepareStatement(DtsConnection dtsConnection, Statement statement) {
    super(dtsConnection, statement);
    this.parameters = Lists.newArrayList();
  }

  @Override
  public PreparedStatement getRawStatement() {
    return (PreparedStatement) super.getRawStatement();
  }

  public final void setNull(final int parameterIndex, final int sqlType) throws SQLException {
    setParameter(parameterIndex, null);
    getRawStatement().setNull(parameterIndex, sqlType);
  }

  @Override
  public final void setNull(final int parameterIndex, final int sqlType, final String typeName)
      throws SQLException {
    setParameter(parameterIndex, null);
    getRawStatement().setNull(parameterIndex, sqlType, typeName);
  }

  @Override
  public final void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setBoolean(parameterIndex, x);
  }

  @Override
  public final void setByte(final int parameterIndex, final byte x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setByte(parameterIndex, x);
  }

  @Override
  public final void setShort(final int parameterIndex, final short x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setShort(parameterIndex, x);
  }

  @Override
  public final void setInt(final int parameterIndex, final int x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setInt(parameterIndex, x);

  }

  @Override
  public final void setLong(final int parameterIndex, final long x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setLong(parameterIndex, x);
  }

  @Override
  public final void setFloat(final int parameterIndex, final float x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setFloat(parameterIndex, x);

  }

  @Override
  public final void setDouble(final int parameterIndex, final double x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setDouble(parameterIndex, x);
  }

  @Override
  public final void setString(final int parameterIndex, final String x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setString(parameterIndex, x);
  }

  @Override
  public final void setBigDecimal(final int parameterIndex, final BigDecimal x)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setBigDecimal(parameterIndex, x);
  }

  @Override
  public final void setDate(final int parameterIndex, final Date x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setDate(parameterIndex, x);

  }

  @Override
  public final void setDate(final int parameterIndex, final Date x, final Calendar cal)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setDate(parameterIndex, x, cal);

  }

  @Override
  public final void setTime(final int parameterIndex, final Time x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setTime(parameterIndex, x);
  }

  @Override
  public final void setTime(final int parameterIndex, final Time x, final Calendar cal)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setTime(parameterIndex, x, cal);
  }

  @Override
  public final void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setTimestamp(parameterIndex, x);
  }

  @Override
  public final void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setTimestamp(parameterIndex, x, cal);
  }

  @Override
  public final void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setBytes(parameterIndex, x);
  }

  @Override
  public final void setBlob(final int parameterIndex, final Blob x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setBlob(parameterIndex, x);
  }

  @Override
  public final void setBlob(final int parameterIndex, final InputStream x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setBlob(parameterIndex, x);
  }

  @Override
  public final void setBlob(final int parameterIndex, final InputStream x, final long length)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setBlob(parameterIndex, x, length);
  }

  @Override
  public final void setClob(final int parameterIndex, final Clob x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setClob(parameterIndex, x);
  }

  @Override
  public final void setClob(final int parameterIndex, final Reader x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setClob(parameterIndex, x);
  }

  @Override
  public final void setClob(final int parameterIndex, final Reader x, final long length)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setClob(parameterIndex, x, length);
  }

  @Override
  public final void setAsciiStream(final int parameterIndex, final InputStream x)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setAsciiStream(parameterIndex, x);
  }

  @Override
  public final void setAsciiStream(final int parameterIndex, final InputStream x, final int length)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setAsciiStream(parameterIndex, x, length);
  }

  @Override
  public final void setAsciiStream(final int parameterIndex, final InputStream x, final long length)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setAsciiStream(parameterIndex, x, length);
  }

  @SuppressWarnings("deprecation")
  @Override
  public final void setUnicodeStream(final int parameterIndex, final InputStream x,
      final int length) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setUnicodeStream(parameterIndex, x, length);
  }

  @Override
  public final void setBinaryStream(final int parameterIndex, final InputStream x)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setBinaryStream(parameterIndex, x);
  }

  @Override
  public final void setBinaryStream(final int parameterIndex, final InputStream x, final int length)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setBinaryStream(parameterIndex, x, length);
  }

  @Override
  public final void setBinaryStream(final int parameterIndex, final InputStream x,
      final long length) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setBinaryStream(parameterIndex, x, length);
  }

  @Override
  public final void setCharacterStream(final int parameterIndex, final Reader x)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setCharacterStream(parameterIndex, x);
  }

  @Override
  public final void setCharacterStream(final int parameterIndex, final Reader x, final int length)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setCharacterStream(parameterIndex, x, length);
  }

  @Override
  public final void setCharacterStream(final int parameterIndex, final Reader x, final long length)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setCharacterStream(parameterIndex, x, length);
  }

  @Override
  public final void setURL(final int parameterIndex, final URL x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setURL(parameterIndex, x);
  }


  @Override
  public final void setSQLXML(final int parameterIndex, final SQLXML x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setSQLXML(parameterIndex, x);
  }

  @Override
  public final void setObject(final int parameterIndex, final Object x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setObject(parameterIndex, x);
  }

  @Override
  public final void setObject(final int parameterIndex, final Object x, final int targetSqlType)
      throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setObject(parameterIndex, x, targetSqlType);
  }

  @Override
  public final void setObject(final int parameterIndex, final Object x, final int targetSqlType,
      final int scaleOrLength) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setObject(parameterIndex, x, targetSqlType, scaleOrLength);
  }

  @Override
  public void setRef(final int parameterIndex, final Ref x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setRef(parameterIndex, x);
  }

  @Override
  public void setArray(final int parameterIndex, final Array x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setArray(parameterIndex, x);
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return getRawStatement().getMetaData();
  }

  @Override
  public ParameterMetaData getParameterMetaData() throws SQLException {
    return getRawStatement().getParameterMetaData();
  }

  @Override
  public void setRowId(final int parameterIndex, final RowId x) throws SQLException {
    setParameter(parameterIndex, x);
    getRawStatement().setRowId(parameterIndex, x);
  }

  @Override
  public void setNString(final int parameterIndex, final String value) throws SQLException {
    setParameter(parameterIndex, value);
    getRawStatement().setNString(parameterIndex, value);
  }

  @Override
  public void setNCharacterStream(final int parameterIndex, final Reader value, final long length)
      throws SQLException {
    setParameter(parameterIndex, value);
    getRawStatement().setNCharacterStream(parameterIndex, value, length);
  }

  @Override
  public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
    setParameter(parameterIndex, value);
    getRawStatement().setNClob(parameterIndex, value);
  }

  @Override
  public void setNClob(final int parameterIndex, final Reader reader, final long length)
      throws SQLException {
    setParameter(parameterIndex, reader);
    getRawStatement().setNClob(parameterIndex, reader, length);
  }

  @Override
  public void setNCharacterStream(final int parameterIndex, final Reader value)
      throws SQLException {
    setParameter(parameterIndex, value);
    getRawStatement().setNCharacterStream(parameterIndex, value);
  }

  @Override
  public void setNClob(final int parameterIndex, final Reader reader) throws SQLException {
    setParameter(parameterIndex, reader);
    getRawStatement().setNClob(parameterIndex, reader);
  }

  @Override
  public final ResultSet executeQuery(final String sql) throws SQLException {
    throw new SQLFeatureNotSupportedException("executeQuery with SQL for PreparedStatement");
  }

  @Override
  public final int executeUpdate(final String sql) throws SQLException {
    throw new SQLFeatureNotSupportedException("executeUpdate with SQL for PreparedStatement");
  }

  @Override
  public final int executeUpdate(final String sql, final int autoGeneratedKeys)
      throws SQLException {
    throw new SQLFeatureNotSupportedException("executeUpdate with SQL for PreparedStatement");
  }

  @Override
  public final int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
    throw new SQLFeatureNotSupportedException("executeUpdate with SQL for PreparedStatement");
  }

  @Override
  public final int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
    throw new SQLFeatureNotSupportedException("executeUpdate with SQL for PreparedStatement");
  }

  @Override
  public final boolean execute(final String sql) throws SQLException {
    throw new SQLFeatureNotSupportedException("execute with SQL for PreparedStatement");
  }

  @Override
  public final boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
    throw new SQLFeatureNotSupportedException("execute with SQL for PreparedStatement");
  }

  @Override
  public final boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
    throw new SQLFeatureNotSupportedException("execute with SQL for PreparedStatement");
  }

  @Override
  public final boolean execute(final String sql, final String[] columnNames) throws SQLException {
    throw new SQLFeatureNotSupportedException("execute with SQL for PreparedStatement");
  }


  private void setParameter(final int parameterIndex, final Object value) {
    if (parameters.size() == parameterIndex - 1) {
      parameters.add(value);
      return;
    }
    for (int i = parameters.size(); i <= parameterIndex - 1; i++) {
      parameters.add(null);
    }
    parameters.set(parameterIndex - 1, value);
  }



  public List<Object> getParameters() {
    return parameters;
  }

  @Override
  public final void clearParameters() throws SQLException {
    parameters.clear();
  }


}
