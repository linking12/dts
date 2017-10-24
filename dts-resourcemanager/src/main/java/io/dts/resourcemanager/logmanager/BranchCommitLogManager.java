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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import io.dts.common.common.CommitMode;
import io.dts.common.common.TxcXID;
import io.dts.common.common.context.ContextStep2;
import io.dts.common.common.exception.DtsException;
import io.dts.parser.constant.SqlType;
import io.dts.parser.constant.UndoLogMode;
import io.dts.parser.model.RollbackInfor;
import io.dts.parser.model.TxcField;
import io.dts.parser.model.TxcRuntimeContext;
import io.dts.parser.model.TxcTable;
import io.dts.resourcemanager.helper.DataSourceHolder;

/**
 * @author liushiming
 * @version BranchCommitLogManager.java, v 0.0.1 2017年10月24日 下午3:52:14 liushiming
 */
public class BranchCommitLogManager extends DtsLogManagerImpl {

  private static final Logger logger = LoggerFactory.getLogger(BranchCommitLogManager.class);

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

  private String getDeleteSql(final TxcTable oriTable) {
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
