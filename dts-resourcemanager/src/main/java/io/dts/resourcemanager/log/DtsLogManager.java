package io.dts.resourcemanager.log;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import io.dts.common.common.CommitMode;
import io.dts.common.common.TxcXID;
import io.dts.common.common.context.ContextStep2;
import io.dts.common.common.exception.DtsException;
import io.dts.common.protocol.ResultCode;
import io.dts.common.util.blob.BlobUtil;
import io.dts.parser.constant.SqlType;
import io.dts.parser.constant.UndoLogMode;
import io.dts.parser.model.RollbackInfor;
import io.dts.parser.model.TxcField;
import io.dts.parser.model.TxcLine;
import io.dts.parser.model.TxcRuntimeContext;
import io.dts.parser.model.TxcTable;
import io.dts.parser.model.TxcTableMeta;
import io.dts.parser.undo.ITxcUndoSqlBuilder;
import io.dts.parser.vistor.support.TxcTableMetaTools;
import io.dts.resourcemanager.help.DataSourceHolder;
import io.dts.resourcemanager.help.SqlExecuteHelper;

/**
 * Created by guoyubo on 2017/9/27.
 */
public class DtsLogManager implements IDtsLogManager {

  private static final Logger logger = LoggerFactory.getLogger(DtsLogManager.class);


  private static String txcLogTableName = "txc_undo_log";

  private static final IDtsLogManager logManager = new DtsLogManager();

  private DtsLogManager() {}


  public static IDtsLogManager getInstance() {
    return logManager;
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

  @Override
  public void branchRollback(final ContextStep2 context) throws SQLException {
    // 根据dbName取注册的datasource
    DataSource datasource = DataSourceHolder.getDataSource(context.getDbname());
    DataSourceTransactionManager tm = new DataSourceTransactionManager(datasource);
    TransactionTemplate transactionTemplate = new TransactionTemplate(tm);
    final JdbcTemplate template = new JdbcTemplate(datasource);

    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          // 查询事务日志
          long gid = TxcXID.getGlobalXID(context.getXid(), context.getBranchId());
          TxcRuntimeContext undolog = getTxcRuntimeContexts(gid, template);
          if (undolog == null) {
            return;
          }

          for (RollbackInfor info : undolog.getInfor()) {
            // 设置表meta
            TxcTable o = info.getOriginalValue();
            TxcTable p = info.getPresentValue();

            String tablename = o.getTableName() == null ? p.getTableName() : o.getTableName();
            TxcTableMeta tablemeta = null;
            try {
              tablemeta = TxcTableMetaTools.getTableMeta("", tablename);
            } catch (Exception e) {
              ; // 吞掉
            }

            if (tablemeta == null) {
              DataSource datasource = null;
              Connection conn = null;
              try {
                datasource = template.getDataSource();
                conn = DataSourceUtils.getConnection(datasource);
                tablemeta = TxcTableMetaTools.getTableMeta(conn, tablename);
              } finally {
                if (conn != null) {
                  DataSourceUtils.releaseConnection(conn, datasource);
                }
              }
            }

            o.setTableMeta(tablemeta);
            p.setTableMeta(tablemeta);
          }

          logger.info(String.format("[logid:%d:xid:%s:branch:%d]", undolog.getId(),
              undolog.getXid(), undolog.getBranchId()));
          for (int i = undolog.getInfor().size(); i > 0; i--) {
            RollbackInfor info = undolog.getInfor().get(i - 1);
            // 检查脏写
            checkDirtyRead(template, info);

            List<String> rollbackSqls =
                ITxcUndoSqlBuilder.createTxcUndoBuilder(info).buildRollbackSql();
            logger.info("the rollback sql is " + rollbackSqls);
            if (!CollectionUtils.isEmpty(rollbackSqls)) {
              template.batchUpdate(rollbackSqls.toArray(new String[rollbackSqls.size()]));
            }

            // 针对不同隔离级别的特殊处理
            // if (TxcResourceManagerImpl.getTxcResourceManager().getIsolationLevel() ==
            // TxcIsolation.READ_COMMITED) {
            // // 回滚
            // switch (info.getSqlType()) {
            // case DELETE:
            // break;
            // default:
            // AbstractUndoSqlBuilder.createTxcUndoExcutor(info).rollback(template);
            // break;
            // }
            //
            // // 刪除事务锁
            //// if (context.getLockMode().getValue() == TrxLockMode.DELETE_TRX_LOCK.getValue()) {
            //// TxcActivityInfo.deleteXLock(undolog.getXid(), template);
            //// }
            // } else {
            // // 回滚
            // AbstractUndoSqlBuilder.createTxcUndoExcutor(info).rollback(template);
            // }
          }

          // 删除undolog
          String deleteSql = getDeleteUndoLogSql(Arrays.asList(context));
          logger.info("delete undo log sql" + deleteSql);
          template.execute(deleteSql);
        } catch (Exception ex) {
          status.setRollbackOnly();
          throw new DtsException(ex, "rollback error");
        }

      }
    });

  }

  private void checkDirtyRead(final JdbcTemplate template, final RollbackInfor info) {
    String selectSql =
        String.format("%s %s FOR UPDATE", info.getSelectSql(), info.getWhereCondition());
    StringBuilder retLog = new StringBuilder();

    long start = 0;
    if (logger.isDebugEnabled())
      start = System.currentTimeMillis();
    try {
      TxcTable p = info.getPresentValue();
      final String valueByLog = p.toString();

      TxcTable t = getDBTxcTable(template, selectSql, p);

      final String valueBySql = t.toString();

      retLog.append("--Log:[");
      retLog.append(valueByLog);
      retLog.append("]");

      retLog.append("--Db[");
      retLog.append(valueBySql);
      retLog.append("]");

      if (valueByLog.equals(valueBySql) == false) {
        throw new DtsException(ResultCode.LOGICERROR.getValue(), "dirty read:" + retLog.toString());
      }
    } catch (Exception e) {
      throw new DtsException(e, "checkDirtyRead error:" + retLog.toString());
    } finally {
      if (logger.isDebugEnabled())
        logger.debug(selectSql + " cost " + (System.currentTimeMillis() - start) + " ms");
    }

  }

  private TxcTable getDBTxcTable(final JdbcTemplate template, final String selectSql,
      final TxcTable p) {
    TxcTable t = new TxcTable();
    t.setTableMeta(p.getTableMeta());
    template.query(selectSql, new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet rs) throws SQLException {
        java.sql.ResultSetMetaData rsmd = rs.getMetaData();
        int column = rsmd.getColumnCount();
        List<TxcField> fields = new ArrayList<TxcField>(column);
        for (int i = 1; i <= column; i++) {
          TxcField field = new TxcField();
          field.setFieldName(rsmd.getColumnName(i));
          field.setFieldType(rsmd.getColumnType(i));
          field.setFieldValue(rs.getObject(i));
          fields.add(field);
        }

        TxcLine line = new TxcLine();
        line.setTableMeta(t.getTableMeta());
        line.setFields(fields);
        t.addLine(line);
      }
    });
    return t;
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
    // 根据dbName取注册的datasource
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
                TxcTable oriTable = infor.getOriginalValue();

                if (oriTable.getLinesNum() == 0) {
                  continue;
                }

                String sql = getDeleteSql(oriTable);
                logger.info("delete sql: " + sql);
                template.execute(sql);
              }
            }
            // 删除事务锁
            // TxcActivityInfo.deleteXLock(c.getXid(), template);
          } catch (Exception ex) {
            status.setRollbackOnly();
            throw new DtsException(ex);
          }
        }


        // 删除undolog
        String deleteSql = getDeleteUndoLogSql(contexts);
        logger.info("delete undo log sql" + deleteSql);
        template.execute(deleteSql);

      }
    });
  }

  private TxcRuntimeContext getTxcRuntimeContexts(final long gid, final JdbcTemplate template) {
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

  private static String getDeleteSql(final TxcTable oriTable) {
    String tableName = oriTable.getTableMeta().getTableName();
    String pkName = oriTable.getTableMeta().getPkName();
    List<TxcField> pkRows = oriTable.pkRows();

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

    return String.format("DELETE FROM %s " + "WHERE %s IN (%s)", tableName, pkName,
        tryDeleteId.toString());
  }


  private static String getDeleteUndoLogSql(final List<ContextStep2> contexts) {
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

    return String.format("delete from %s where id in (%s) and status = %d", txcLogTableName,
        sb.toString(), UndoLogMode.COMMON_LOG.getValue());
  }

  @Override
  public void deleteUndoLog(ContextStep2 context, JdbcTemplate template) throws SQLException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteUndoLog(List<ContextStep2> contexts, JdbcTemplate template)
      throws SQLException {
    // TODO Auto-generated method stub

  }

}
