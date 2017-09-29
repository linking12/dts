package io.dts.resourcemanager.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import io.dts.common.context.ContextStep2;
import io.dts.parser.model.TxcRuntimeContext;

/**
 * Created by guoyubo on 2017/9/27.
 */
public interface ITxcLogManager {

  void branchCommit(List<ContextStep2> contexts) throws SQLException;

  Integer insertUndoLog(Connection connection, TxcRuntimeContext txcContext) throws SQLException;
}
