package io.dts.datasource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import io.dts.common.common.CommitMode;
import io.dts.common.common.TxcXID;
import io.dts.common.common.context.DtsContext;
import io.dts.datasource.wrapper.DtsPrepareStatementWrapper;
import io.dts.datasource.wrapper.DtsStatementWrapper;
import io.dts.parser.constant.UndoLogMode;
import io.dts.parser.model.TxcRuntimeContext;
import io.dts.resourcemanager.api.IDtsDataSource;
import io.dts.resourcemanager.logmanager.DtsLogManagerImpl;

/**
 * Created by guoyubo on 2017/9/20.
 */
public class DtsConnection extends AbstractDtsConnection {

  private DtsDataSource dtsDataSource;

  private Connection connection;

  private TxcRuntimeContext txcContext; // 事务SQL上下文


  public DtsConnection(final DtsDataSource dtsDataSource, final Connection connection)
      throws SQLException {
    this.dtsDataSource = dtsDataSource;
    this.connection = connection;
  }

  @Override
  public Statement createStatement() throws SQLException {
    return new DtsStatementWrapper(this, getRawConnection().createStatement());
  }


  @Override
  public PreparedStatement prepareStatement(final String sql) throws SQLException {
    return new DtsPrepareStatementWrapper(this, getRawConnection().prepareStatement(sql), sql);
  }

  @Override
  public CallableStatement prepareCall(final String sql) throws SQLException {
    return null;
  }


  @Override
  public void setAutoCommit(final boolean autoCommit) throws SQLException {
    getRawConnection().setAutoCommit(autoCommit);
    if (!autoCommit) {
      registerBranch();
    }
  }

  private void registerBranch() throws SQLException {
    if (!DtsContext.inTxcTransaction()) {
      return;
    }
    if (getAutoCommit() == true) {
      throw new SQLException("should set autocommit false first.");
    }
    long branchId = dtsDataSource.getResourceManager().register(dtsDataSource.getDbName(),
        CommitMode.COMMIT_IN_PHASE1);
    txcContext = new TxcRuntimeContext();
    txcContext.setBranchId(branchId);
    txcContext.setXid(DtsContext.getCurrentXid());
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return getRawConnection().getAutoCommit();
  }

  @Override
  public void commit() throws SQLException {
    try {
      if (DtsContext.inTxcTransaction()) {
        // 日志写库
        txcContext.setServer(TxcXID.getServerAddress(txcContext.getXid()));
        txcContext.setStatus(UndoLogMode.COMMON_LOG.getValue());
        DtsLogManagerImpl.insertUndoLog(this.getRawConnection(), txcContext);
        getRawConnection().commit();
        reportBranchStatus(true);
      } else {
        getRawConnection().commit();
      }
    } finally {
      txcContext = null;
    }
  }

  private void reportBranchStatus(final boolean success) {
    if (DtsContext.inTxcTransaction()) {
      dtsDataSource.getResourceManager().reportStatus(txcContext.getBranchId(), success,
          dtsDataSource.getDbName(), null);
    }
  }

  @Override
  public void rollback() throws SQLException {
    try {
      if (DtsContext.inTxcTransaction()) {
        getRawConnection().rollback();
        reportBranchStatus(false);
      } else {
        getRawConnection().rollback();
      }
    } finally {
      txcContext = null;
    }
  }

  @Override
  public void close() throws SQLException {
    try {
      getRawConnection().close();
    } finally {
      txcContext = null;
    }
  }

  @Override
  public boolean isClosed() throws SQLException {
    return getRawConnection().isClosed();
  }

  @Override
  public Statement createStatement(final int resultSetType, final int resultSetConcurrency)
      throws SQLException {
    return new DtsStatementWrapper(this,
        getRawConnection().createStatement(resultSetType, resultSetConcurrency));
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int resultSetType,
      final int resultSetConcurrency) throws SQLException {
    return null;
  }

  @Override
  public CallableStatement prepareCall(final String sql, final int resultSetType,
      final int resultSetConcurrency) throws SQLException {
    return null;
  }

  @Override
  public Statement createStatement(final int resultSetType, final int resultSetConcurrency,
      final int resultSetHoldability) throws SQLException {
    return new DtsStatementWrapper(this, getRawConnection().createStatement(resultSetType,
        resultSetConcurrency, resultSetHoldability));
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int resultSetType,
      final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
    return null;
  }

  @Override
  public CallableStatement prepareCall(final String sql, final int resultSetType,
      final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
    return null;
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys)
      throws SQLException {
    return null;
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes)
      throws SQLException {
    return null;
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final String[] columnNames)
      throws SQLException {
    return null;
  }

  @Override
  public Connection getRawConnection() throws SQLException {
    return connection;
  }

  @Override
  public IDtsDataSource getDataSource() throws SQLException {
    return dtsDataSource;
  }

  @Override
  public TxcRuntimeContext getTxcContext() {
    return txcContext;
  }
}
