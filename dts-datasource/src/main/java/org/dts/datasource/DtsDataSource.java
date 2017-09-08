package org.dts.datasource;

import org.dts.datasource.filter.BranchRegisterFilter;
import org.dts.datasource.filter.StatementExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.google.common.collect.Lists;
import com.quancheng.dts.common.CommitMode;
import com.quancheng.dts.common.DtsContext;
import com.quansheng.dts.resourcemanager.DtsResourceManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by guoyubo on 2017/8/22.
 */
public class DtsDataSource extends DruidDataSource {

  private static final Logger log = LoggerFactory.getLogger(DtsDataSource.class);

  private ThreadLocal<ConcurrentHashMap<ConnectionProxy, Long>> statementHolder = new ThreadLocal<>();

  private DtsResourceManager resourceManager;

  public DtsDataSource() {
    super();
  }

  public DtsDataSource(final boolean fairLock) {
    super(fairLock);
  }

  @Override
  public void init() throws SQLException {
    final List<StatementExecuteListener> statementExecuteListeners = new ArrayList<>();
    statementExecuteListeners.add(new StatementExecuteListener() {

      @Override
      public void afterStatementCreate(final StatementProxy statement) {


      }

      @Override
      public void afterSetAutoCommit(final ConnectionProxy connection, final boolean autoCommit) {
        if (DtsContext.inTxcTransaction() && !autoCommit) {
          long branchId = resourceManager.register(DtsDataSource.this.getName(), CommitMode.COMMIT_IN_PHASE1);
          DtsDataSource.this.bindBranchId(connection, branchId);
        }
      }

      @Override
      public void beforeExecute(final StatementProxy statement, final String sql) {
        if (DtsContext.inTxcTransaction()) {
        }
      }

      @Override
      public void afterExecute(final StatementProxy statement, String sql, final Throwable error) {
        if (DtsContext.inRetryContext()) {
          if (error != null) {
          }
        } else {
          if (error != null) {
            resourceManager.reportStatus(DtsDataSource.this.getBindBranchId(statement), false, DtsDataSource.this.getName(), null);
          } else {
            resourceManager.reportStatus(DtsDataSource.this.getBindBranchId(statement), true, DtsDataSource.this.getName(), null);
          }
        }
      }


    });
    super.setProxyFilters(Lists.newArrayList(new BranchRegisterFilter(statementExecuteListeners)));
    super.init();
  }

  private void bindBranchId(final ConnectionProxy connectionProxy, final long branchId) {
    if (statementHolder.get() == null) {
      ConcurrentHashMap<ConnectionProxy, Long> concurrentHashMap = new ConcurrentHashMap();
      concurrentHashMap.put(connectionProxy, branchId);
      statementHolder.set(concurrentHashMap);
    } else {
      ConcurrentHashMap<ConnectionProxy, Long> concurrentHashMap = statementHolder.get();
      concurrentHashMap.put(connectionProxy, branchId);
    }
  }


  private Long getBindBranchId(final StatementProxy statement) {
    Long branchId = statementHolder.get().get(statement.getConnectionProxy());
    return branchId;
  }

}
