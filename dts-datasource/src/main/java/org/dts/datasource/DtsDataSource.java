package org.dts.datasource;

import org.dts.datasource.filter.BranchRegisterFilter;
import org.dts.datasource.filter.StatementExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.google.common.collect.Lists;
import com.quancheng.dts.common.CommitMode;
import com.quancheng.dts.common.DtsContext;
import com.quansheng.dts.resourcemanager.DtsResourceManager;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by guoyubo on 2017/8/22.
 */
public class DtsDataSource extends DruidDataSource {

  private static final Logger log = LoggerFactory.getLogger(DtsDataSource.class);

  private ThreadLocal<ConcurrentHashMap<Statement, Long>> statementHolder = new ThreadLocal<>();

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
        String sql = statement.getSqlStat().getSql();
        if (sql.startsWith("select") || sql.startsWith("SELECT")) {
          return;
        }
        if (DtsContext.inTxcTransaction()) {
          long branchId = resourceManager.register(DtsDataSource.this.getName(), CommitMode.COMMIT_IN_PHASE1);
          DtsDataSource.this.bindBranchId(statement, branchId);
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

  private void bindBranchId(final StatementProxy statement, final long branchId) {
    if (statementHolder.get() == null) {
      ConcurrentHashMap<Statement, Long> concurrentHashMap = new ConcurrentHashMap();
      concurrentHashMap.put(statement, branchId);
      statementHolder.set(concurrentHashMap);
    } else {
      ConcurrentHashMap<Statement, Long> concurrentHashMap = statementHolder.get();
      concurrentHashMap.put(statement, branchId);
    }
  }


  private Long getBindBranchId(final StatementProxy statement) {
    Long branchId = statementHolder.get().get(statement);
    return branchId;
  }

}
