package com.quansheng.dts.resourcemanager.undo;

import com.quancheng.dts.common.DtsXID;
import com.quancheng.dts.exception.DtsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by guoyubo on 2017/9/12.
 */
@Slf4j
public class UndoLogManager {

  private static String txcLogTableName = "txc_undo_log";

  public int insertUndoLog(Connection conn, String xid, Long branchId, String serverAddr,
      final int status) throws DtsException {
    long globalXid = DtsXID.getGlobalXID(xid, branchId);

    // insert sql
    StringBuilder insertSql = new StringBuilder("INSERT INTO ");
    insertSql.append(txcLogTableName);
    insertSql.append("(id, xid, branch_id, rollback_info, ");
    insertSql.append("gmt_create, gmt_modified, status, server)");
    insertSql.append(" VALUES(");
    insertSql.append("?,"); // id
    insertSql.append("?,"); // xid
    insertSql.append("?,"); // branch_id
    insertSql.append("?,"); // rollback_info
    insertSql.append("now(),"); // gmt_create
    insertSql.append("now(),"); // gmt_modified
    insertSql.append(status); // status
    insertSql.append(",?)"); // server

    PreparedStatement pst = null;
    try {
      pst = conn.prepareStatement(insertSql.toString());
      pst.setLong(1, globalXid);
      pst.setString(2, xid);
      pst.setLong(3, branchId);
//      pst.setBlob(4, BlobUtil.string2blob(txcLog.encode()));
      pst.setString(5, serverAddr);
      return pst.executeUpdate();
    } catch (Exception e) {
      log.error("insert undo log error", e);
      return 0;
    } finally {
      if (pst != null) {
        try {
          pst.close();
        } catch (SQLException e) {
          log.error("insert undo log error", e);
         return 0;
        }
      }
    }
  }

}
