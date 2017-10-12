package io.dts.resourcemanager.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.dts.common.common.CommitMode;
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
import io.dts.resourcemanager.core.IDtsLogManager;
import io.dts.resourcemanager.support.DataSourceHolder;
import io.dts.resourcemanager.support.SqlExecuteHelper;

/**
 * Created by guoyubo on 2017/9/27.
 */
public class DtsLogManager implements IDtsLogManager {

  private static final Logger logger = LoggerFactory.getLogger(DtsLogManager.class);


  private static String txcLogTableName = "txc_undo_log";


  @Override
  public Integer insertUndoLog(final Connection connection, final TxcRuntimeContext txcContext) throws SQLException {
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

    return SqlExecuteHelper.executeSql(connection, insertSql.toString(), new PreparedStatementCallback<Integer>() {
      @Override
      public Integer doInPreparedStatement(final PreparedStatement pst)
          throws SQLException {
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

  @Override
  public void branchCommit(List<ContextStep2> contexts) throws SQLException {
    // RT
    Iterator<ContextStep2> it = contexts.iterator();
    while (it.hasNext()) {
      ContextStep2 c = it.next();
      if (c.getCommitMode().getValue() == CommitMode.COMMIT_RETRY_MODE.getValue()) {
        SqlExecuteHelper.executeSql(c.getDbname(), c.getRetrySql());
        it.remove();
      }
    }

    // AT
    Map<String, List<ContextStep2>> maps = new HashMap<String, List<ContextStep2>>();
    for (ContextStep2 c : contexts) {
      List<ContextStep2> list = maps.get(c.getDbname());
      if (list == null) {
        list = new ArrayList<ContextStep2>();
        maps.put(c.getDbname(), list);
      }
      list.add(c);
    }

    for (Map.Entry<String, List<ContextStep2>> entry : maps.entrySet()) {
      String dbname = entry.getKey();
      List<ContextStep2> ids = entry.getValue();
      branchCommit(ids, dbname);
    }
  }

  private void branchCommit(List<ContextStep2> contexts, String dbName) throws SQLException {
    // 首先转换datasource为TDDL的datasource，回滚逻辑不需要记UndoLog
    DataSource datasource = DataSourceHolder.getDataSource(dbName);
    DataSourceTransactionManager tm = new DataSourceTransactionManager(datasource);
    TransactionTemplate transactionTemplate = new TransactionTemplate(tm);
    final JdbcTemplate template = new JdbcTemplate(datasource);

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        for (final ContextStep2 c : contexts) {
          try {
            long gid = TxcXID.getGlobalXID(c.getXid(), c.getBranchId());

            // 针对delete操作延迟删除数据
            TxcRuntimeContext undos = getTxcRuntimeContexts(gid, template);
            if (undos == null) {
              return;
            }

            //
            for (RollbackInfor infor : undos.getInfor()) {
              if (infor.getSqlType() == SqlType.DELETE) {
                delayDelete(infor, template);
              }
            }
            // 删除事务锁
  //            TxcActivityInfo.deleteXLock(c.getXid(), template);
          } catch (Exception ex) {
            status.setRollbackOnly();
            throw new DtsException(ex);
          }
        }


        // 删除undolog
        deleteUndoLog(contexts, template);

      }
    });
  }

  private TxcRuntimeContext getTxcRuntimeContexts(final long gid, final JdbcTemplate template) {
    String sql = String.format("select * from %s where status = 0 && " + "id = %d order by id desc", txcLogTableName, gid);
    List<TxcRuntimeContext> undos = SqlExecuteHelper.querySql(template, new RowMapper() {
      @Override
      public TxcRuntimeContext mapRow(ResultSet rs, int rowNum) throws SQLException {
        Blob blob = rs.getBlob("rollback_info");
        String str = BlobUtil.blob2string(blob);
        TxcRuntimeContext undoLogInfor = TxcRuntimeContext.decode(str);
        return undoLogInfor;
      }
    },  sql);

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

  private static void deleteUndoLog(final List<ContextStep2> contexts, final JdbcTemplate template) {
    StringBuilder sb = new StringBuilder();
    boolean flag = false;
    for (ContextStep2 c : contexts) {
      if (flag == true) {
        sb.append(",");
      } else {
        flag = true;
      }
      sb.append(c.getGlobalXid());
    }

    String deleteSql = String.format("delete from %s where id in (%s) and status = %d", txcLogTableName, sb.toString(), UndoLogMode.COMMON_LOG.getValue());
    logger.info(deleteSql);
    template.execute(deleteSql);

  }


}
