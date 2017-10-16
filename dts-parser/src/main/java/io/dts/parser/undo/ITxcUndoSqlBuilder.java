package io.dts.parser.undo;

import java.util.List;

import io.dts.common.common.exception.DtsException;
import io.dts.parser.model.RollbackInfor;
import io.dts.parser.undo.impl.TxcDeleteUndoSqlBuilder;
import io.dts.parser.undo.impl.TxcInsertUndoSqlBuilder;
import io.dts.parser.undo.impl.TxcUpdateUndoSqlBuilder;

/**
 * Created by guoyubo on 2017/10/16.
 */
public interface ITxcUndoSqlBuilder {

  List<String> buildRollbackSql();


  public static ITxcUndoSqlBuilder createTxcUndoBuilder(RollbackInfor txcUndoLogRollbackInfor) {
    ITxcUndoSqlBuilder undo = null;
    switch (txcUndoLogRollbackInfor.getSqlType()) {
      case DELETE:
        undo = new TxcDeleteUndoSqlBuilder(txcUndoLogRollbackInfor);
        break;
      case INSERT:
        undo = new TxcInsertUndoSqlBuilder(txcUndoLogRollbackInfor);
        break;
      case UPDATE:
        undo = new TxcUpdateUndoSqlBuilder(txcUndoLogRollbackInfor);
        break;
      default:
        throw new DtsException("sqltype error:" + txcUndoLogRollbackInfor.getSqlType());
    }

    return undo;
  }

}
