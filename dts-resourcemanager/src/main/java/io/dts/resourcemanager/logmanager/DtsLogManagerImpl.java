/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.dts.resourcemanager.logmanager;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;

import io.dts.common.common.TxcXID;
import io.dts.common.common.context.ContextStep2;
import io.dts.common.common.exception.DtsException;
import io.dts.common.util.blob.BlobUtil;
import io.dts.parser.struct.TxcRuntimeContext;
import io.dts.resourcemanager.helper.DataSourceHolder;

/**
 * @author liushiming
 * @version AbstractLogManager.java, v 0.0.1 2017年10月24日 下午3:48:10 liushiming
 */
public class DtsLogManagerImpl implements DtsLogManager {


  protected static final String txcLogTableName = "txc_undo_log";

  protected static DtsLogManager logManager = new DtsLogManagerImpl();

  private volatile BranchRollbackLogManager rollbackLogManager;

  private volatile BranchCommitLogManager commitLogManager;

  protected DtsLogManagerImpl() {}

  /**
   * 分支事务提交，仅删除UndoLog
   */
  @Override
  public void branchCommit(List<ContextStep2> contexts) throws SQLException {
    if (commitLogManager == null) {
      commitLogManager = new BranchCommitLogManager();
    }
    commitLogManager.branchCommit(contexts);
  }

  /**
   * 分支事务回滚，回滚阶段的数据库操作在一个本地事务中执行
   */
  @Override
  public void branchRollback(ContextStep2 context) throws SQLException {
    if (rollbackLogManager == null) {
      rollbackLogManager = new BranchRollbackLogManager();
    }
    rollbackLogManager.branchRollback(context);
  }

  protected TxcRuntimeContext getTxcRuntimeContexts(final long gid, final JdbcTemplate template) {
    String sql = String.format("select * from %s where status = 0 && " + "id = %d order by id desc",
        txcLogTableName, gid);
    List<TxcRuntimeContext> undos = SqlExecuteHelper.querySql(template, new RowMapper() {
      @Override
      public TxcRuntimeContext mapRow(ResultSet rs, int rowNum) throws SQLException {
        Blob blob = rs.getBlob("rollback_info");
        String str = BlobUtil.blob2string(blob);
        TxcRuntimeContext undoLogInfor = TxcRuntimeContext.decode(str);
        return undoLogInfor;
      }
    }, sql);

    if (undos == null) {
      return null;
    }

    if (undos.size() == 0) {
      return null;
    }

    if (undos.size() > 1) {
      throw new DtsException("check txc_undo_log, trx info duplicate");
    }
    return undos.get(0);
  }

  public static Integer insertUndoLog(final Connection connection,
      final TxcRuntimeContext txcContext) throws SQLException {
    String xid = txcContext.getXid();
    long branchID = txcContext.getBranchId();
    long globalXid = TxcXID.getGlobalXID(xid, branchID);
    String serverAddr = txcContext.getServer();
    StringBuilder insertSql = new StringBuilder("INSERT INTO ");
    insertSql.append(txcLogTableName);
    insertSql.append("(id, xid, branch_id, rollback_info, ");
    insertSql.append("gmt_create, gmt_modified, status, server)");
    insertSql.append(" VALUES(");
    insertSql.append("?,"); // id
    insertSql.append("?,"); // xid
    insertSql.append("?,"); // branch_id
    insertSql.append("?,"); // rollback_info
    insertSql.append("?,"); // gmt_create
    insertSql.append("?,"); // gmt_modified
    insertSql.append(txcContext.getStatus()); // status
    insertSql.append(",?)"); // server
    return SqlExecuteHelper.executeSql(connection, insertSql.toString(),
        new PreparedStatementCallback<Integer>() {
          @Override
          public Integer doInPreparedStatement(final PreparedStatement pst) throws SQLException {
            pst.setLong(1, globalXid);
            pst.setString(2, xid);
            pst.setLong(3, branchID);
            pst.setBlob(4, BlobUtil.string2blob(txcContext.encode()));
            java.sql.Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());
            pst.setTimestamp(5, currentTime);
            pst.setTimestamp(6, currentTime);
            pst.setString(7, serverAddr);
            return pst.executeUpdate();
          }
        });

  }



  static class SqlExecuteHelper {

    private static final Logger logger = LoggerFactory.getLogger(SqlExecuteHelper.class);

    public static void executeSql(String dbName, String sql) throws SQLException {
      if (sql == null) {
        return;
      }
      try {
        DataSource db = DataSourceHolder.getDataSource(dbName);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
        jdbcTemplate.execute(sql);
      } catch (DataAccessException e) {
        SQLException sqle = (SQLException) e.getCause();
        if (sqle.getErrorCode() == 1062) {
          logger.info("RtExecutor retry sql:" + e.getMessage() + ":" + sql);
        } else {
          throw sqle;
        }
      }
    }

    public static <T> List<T> querySql(final JdbcTemplate template, final RowMapper rowMapper,
        final String sql) {
      List<T> contents;
      long start = 0;
      if (logger.isDebugEnabled())
        start = System.currentTimeMillis();
      try {
        contents = template.query(sql, rowMapper);
      } finally {
        if (logger.isDebugEnabled()) {
          long end = System.currentTimeMillis();
          logger.info(String.format("query:[%s] cost %d ms.", sql, (end - start)));
        }
      }

      return contents;
    }


    public static <T> T executeSql(final Connection connection, String sql,
        PreparedStatementCallback<T> callback) throws SQLException {
      PreparedStatement pst = null;
      try {
        pst = connection.prepareStatement(sql);
        return callback.doInPreparedStatement(pst);
      } finally {
        if (pst != null) {
          pst.close();
        }
      }
    }

  }
}
