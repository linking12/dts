package io.dts.resourcemanager.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import io.dts.common.common.TxcXID;
import io.dts.common.context.ContextStep2;
import io.dts.common.exception.DtsException;
import io.dts.common.util.BlobUtil;
import io.dts.parser.constant.SqlType;
import io.dts.parser.constant.UndoLogMode;
import io.dts.parser.model.RollbackInfor;
import io.dts.parser.model.TxcField;
import io.dts.parser.model.TxcRuntimeContext;
import io.dts.parser.model.TxcTable;

/**
 * Created by guoyubo on 2017/9/27.
 */
public class SqlExecuteHelper {

  private static final Logger logger = LoggerFactory.getLogger(SqlExecuteHelper.class);

  private static String txcLogTableName = "txc_undo_log";


  public static void executeSql(String dbName, String retrySql) throws SQLException {
    if (retrySql == null) {
      return;
    }
    try {
      DataSource db = DataSourceHolder.getDataSource(dbName);
      JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
      jdbcTemplate.execute(retrySql);
    } catch (DataAccessException e) {
      // SQLState:23000
      // VendorCode:1062
      // Duplicate entry key 'PRIMARY'
      SQLException sqle = (SQLException) e.getCause();
      if (sqle.getErrorCode() == 1062) {
        logger.info("RtExecutor retry sql:" + e.getMessage() + ":" + retrySql);
      } else {
        throw sqle;
      }
    }
  }

  public static void deleteUndoLogs(String dbName, final List<ContextStep2> contexts) {
    // 首先转换datasource为TDDL的datasource，回滚逻辑不需要记UndoLog
    DataSource datasource = DataSourceHolder.getDataSource(dbName);
    DataSourceTransactionManager tm = new DataSourceTransactionManager(datasource);
    TransactionTemplate transactionTemplate = new TransactionTemplate(tm);
    final JdbcTemplate template = new JdbcTemplate(datasource);

    if (logger.isDebugEnabled()) {
      for (final ContextStep2 c : contexts) {
        logger.debug(String.format("%s branchCommit [%s]", TxcXID.formatXid(c.getXid(), c.getBranchId()), dbName));
      }
    }
    for (final ContextStep2 c : contexts) {
      transactionTemplate.execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          try {
            // 针对delete操作延迟删除数据
            TxcRuntimeContext undos = selectUndoLog(c.getXid(), c.getBranchId(), template);
            if (undos == null) {
              return;
            }

            for (RollbackInfor infor : undos.getInfor()) {
              if (infor.getSqlType() == SqlType.DELETE) {
                delayDelete(infor, template);
              }
            }

            // 删除undolog
            deleteUndoLog(c, template);

            // 删除事务锁
//            TxcActivityInfo.deleteXLock(c.getXid(), template);
          } catch (Exception ex) {
            status.setRollbackOnly();

              throw new DtsException(ex);
          }
        }
      });
    }
  }

  private static void delayDelete(final RollbackInfor txcLog, final JdbcTemplate template) {
    TxcTable oriTable = txcLog.getOriginalValue();
    String tableName = oriTable.getTableMeta().getTableName();
    String pkName = oriTable.getTableMeta().getPkName();
    List<TxcField> pkRows = oriTable.pkRows();

    if (oriTable.getLinesNum() == 0) {
      return;
    }

    boolean firstFlag = true;
    StringBuilder tryDeleteId = new StringBuilder();
    for (TxcField field : pkRows) {
      if (firstFlag) {
        firstFlag = false;
      } else {
        tryDeleteId.append(",");
      }
      tryDeleteId.append(field.getFieldValue());
    }

    String sql = String.format("DELETE FROM %s " + "WHERE %s IN (%s)", tableName, pkName, tryDeleteId.toString());
    logger.info(sql);
    template.execute(sql);
  }

  private static void deleteUndoLog(final ContextStep2 contexts, final JdbcTemplate template) {
    StringBuilder sb = new StringBuilder();
    boolean flag = false;
    for (ContextStep2 c : (List<ContextStep2>) contexts) {
      if (flag == true) {
        sb.append(",");
      } else {
        flag = true;
      }
      sb.append(c.getGlobalXid());
    }

    String deleteSql = String.format("delete from %s where id in (%s) and status = %d", txcLogTableName, sb.toString(), UndoLogMode.COMMON_LOG.getValue());
    long start = 0;
    if (logger.isDebugEnabled())
      start = System.currentTimeMillis();
    template.execute(deleteSql);

    // 日志
    if (logger.isDebugEnabled()) {
      long end = System.currentTimeMillis();
      logger.debug(String.format("deleteUndoLog batch:[%s] cost %d ms.", deleteSql, (end - start)));
    }
  }

  private static TxcRuntimeContext selectUndoLog(final String xid, final long branchId, final JdbcTemplate template) {
    long gid = TxcXID.getGlobalXID(xid, branchId);
    String sql = String.format("select * from %s where status = 0 && " + "id = %d order by id desc", txcLogTableName, gid);

    List<TxcRuntimeContext> contents;
    long start = 0;
    if (logger.isDebugEnabled())
      start = System.currentTimeMillis();
    try {
      contents = template.query(sql, new RowMapper() {
        @Override
        public TxcRuntimeContext mapRow(ResultSet rs, int rowNum) throws SQLException {
          Blob blob = rs.getBlob("rollback_info");
          String str = BlobUtil.blob2string(blob);
          TxcRuntimeContext undoLogInfor = TxcRuntimeContext.decode(str);
          return undoLogInfor;
        }
      });
    } finally {
      if (logger.isDebugEnabled()) {
        long end = System.currentTimeMillis();
        logger.info(String.format("selectUndoLog:[%s] cost %d ms.", sql, (end - start)));
      }
    }

    if (contents == null) {
      return null;
    }

    if (contents.size() == 0) {
      return null;
    }

    if (contents.size() > 1) {
      throw new DtsException("check txc_undo_log, trx info duplicate");
    }

    return contents.get(0);
  }


  public static void insertUndoLog(final Connection connection, TxcRuntimeContext txcLog) throws SQLException {
    String xid = txcLog.getXid();
    long branchID = txcLog.getBranchId();
    long globalXid = TxcXID.getGlobalXID(xid, branchID);
    String serverAddr = txcLog.getServer();

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
    insertSql.append(txcLog.getStatus()); // status
    insertSql.append(",?)"); // server

    PreparedStatement pst = null;
    try {
      pst = connection.prepareStatement(insertSql.toString());
      pst.setLong(1, globalXid);
      pst.setString(2, xid);
      pst.setLong(3, branchID);
      pst.setBlob(4, BlobUtil.string2blob(txcLog.encode()));
      pst.setString(5, serverAddr);
      pst.executeUpdate();
    } finally {
      if (pst != null) {
        pst.close();
      }
      if (logger.isDebugEnabled())
        logger.debug(String.format("%s insertUndoLog cost %d ms", TxcXID.formatXid(xid, branchID), txcLog.getRTFromLastPoint()));
    }

  }
}
