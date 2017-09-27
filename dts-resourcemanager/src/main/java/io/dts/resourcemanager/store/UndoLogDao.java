package io.dts.resourcemanager.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import io.dts.common.common.TxcXID;
import io.dts.common.util.BlobUtil;
import io.dts.parser.model.TxcRuntimeContext;

/**
 * Created by guoyubo on 2017/9/20.
 */
public class UndoLogDao {

  private Connection connection;

  private final String txcLogTableName = "txc_undo_log";

  public UndoLogDao(final Connection connection) {
    this.connection = connection;
  }

  public  void insertUndoLog(TxcRuntimeContext txcLog) throws SQLException {
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
      java.sql.Date currentTime = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
      pst.setDate(5, currentTime);
      pst.setDate(6, currentTime);
      pst.setString(7, serverAddr);
      pst.executeUpdate();
    } finally {
      if (pst != null) {
        pst.close();
      }
    }
  }
}
