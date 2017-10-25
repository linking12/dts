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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import io.dts.common.common.TxcXID;
import io.dts.common.common.context.ContextStep2;
import io.dts.common.common.exception.DtsException;
import io.dts.common.protocol.ResultCode;
import io.dts.parser.constant.UndoLogMode;
import io.dts.parser.model.RollbackInfor;
import io.dts.parser.model.TxcField;
import io.dts.parser.model.TxcLine;
import io.dts.parser.model.TxcRuntimeContext;
import io.dts.parser.model.TxcTable;
import io.dts.parser.model.TxcTableMeta;
import io.dts.parser.vistor.support.TxcTableMetaTools;
import io.dts.resourcemanager.helper.DataSourceHolder;
import io.dts.resourcemanager.undo.DtsUndo;

/**
 * @author liushiming
 * @version BranchRollbackLogManager.java, v 0.0.1 2017年10月24日 下午3:57:23 liushiming
 */
public class BranchRollbackLogManager extends DtsLogManagerImpl {
  private static final Logger logger = LoggerFactory.getLogger(BranchRollbackLogManager.class);

  @Override
  public void branchRollback(ContextStep2 context) throws SQLException {
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
                DtsUndo.createDtsundo(info).buildRollbackSql();
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

  private String getDeleteUndoLogSql(final List<ContextStep2> contexts) {
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
}
