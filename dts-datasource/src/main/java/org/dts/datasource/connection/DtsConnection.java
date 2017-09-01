package org.dts.datasource.connection;

import org.dts.datasource.DtsDataSource;

import com.quancheng.dts.common.DtsContext;
import com.quancheng.dts.message.request.BeginRetryBranchMessage;
import com.quancheng.dts.rpc.DtsClientMessageSender;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoyubo on 2017/7/25.
 */
public class DtsConnection extends DtsAbstractConnection {

  private DtsDataSource dtsDataSource;

  private final List<Connection> connections = new ArrayList<>();


  public DtsConnection(final DtsDataSource dtsDataSource) {
    this.dtsDataSource = dtsDataSource;
  }

  @Override
  public Connection getInternalConnection() throws SQLException {
    Connection connection = dtsDataSource.getConnection();
    connections.add(connection);
    return connection;
  }

  @Override
  public void commit() throws SQLException {
    for (Connection each : getConnections()) {
      each.commit();
    }
  }

  @Override
  public void rollback() throws SQLException {
    for (Connection each : getConnections()) {
      each.rollback();
    }
  }

  @Override
  public void close() throws SQLException {
    for (Connection connection : connections) {
      connection.close();
    }
  }

  public List<Connection> getConnections() {
    return connections;
  }
}
