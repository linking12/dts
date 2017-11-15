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
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.PreparedStatementCallback;

import io.dts.common.context.DtsXID;
import io.dts.common.util.BlobUtil;
import io.dts.parser.struct.TxcRuntimeContext;
import io.dts.resourcemanager.struct.ContextStep2;

/**
 * @author liushiming
 * @version DtsLogManager.java, v 0.0.1 2017年10月24日 下午3:22:15 liushiming
 */
public interface DtsLogManager {

  public static final String txcLogTableName = "dts_undo_log";

  /**
   * 分支事务提交，仅删除UndoLog
   */
  void branchCommit(ContextStep2 context) throws SQLException;

  /**
   * 分支事务回滚，回滚阶段的数据库操作在一个本地事务中执行
   */
  void branchRollback(ContextStep2 context) throws SQLException;



  default Integer insertUndoLog(final Connection connection, final TxcRuntimeContext txcContext)
      throws SQLException {
    String xid = txcContext.getXid();
    long branchID = txcContext.getBranchId();
    long globalXid = DtsXID.getGlobalXID(xid, branchID);
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

  public static DtsLogManager getInstance() {
    return DtsLogManagerImpl.logManager;
  }
}
