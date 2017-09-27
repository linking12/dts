package io.dts.resourcemanager.core.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.dts.common.common.CommitMode;
import io.dts.common.context.ContextStep2;
import io.dts.parser.model.TxcRuntimeContext;
import io.dts.resourcemanager.core.ITxcLogManager;
import io.dts.resourcemanager.support.SqlExecuteHelper;

/**
 * Created by guoyubo on 2017/9/27.
 */
public class TxcLogManager implements ITxcLogManager {


  @Override
  public void insertUndoLog(final Connection connection, final TxcRuntimeContext txcContext) throws SQLException {
    SqlExecuteHelper.insertUndoLog(connection, txcContext);
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
    SqlExecuteHelper.deleteUndoLogs(dbName, contexts);
  }
}
