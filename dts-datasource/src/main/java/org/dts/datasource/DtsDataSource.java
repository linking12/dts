package org.dts.datasource;

import org.dts.datasource.connection.DtsConnection;

import com.quancheng.dts.rpc.DtsClientMessageSender;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by guoyubo on 2017/8/22.
 */
public class DtsDataSource extends AbstractDataSource {

  private String dbKey;

  private DtsClientMessageSender clientMessageSender;


  public DtsDataSource(final String dbKey) {
    this.dbKey = dbKey;
  }

  @Override
  public Connection getConnection(final String username, final String password) throws SQLException {
    throw new UnsupportedOperationException("Not supported by DianrongDataSource");
  }

  @Override
  public Connection getConnection() throws SQLException {
    return new DtsConnection(this);
  }

  public String getDbKey() {
    return dbKey;
  }

  public DataSource getDataSource() {
    return null;
  }
}
