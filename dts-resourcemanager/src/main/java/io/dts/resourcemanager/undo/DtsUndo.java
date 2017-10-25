package io.dts.resourcemanager.undo;

import java.util.List;

import io.dts.common.common.exception.DtsException;
import io.dts.parser.struct.RollbackInfor;

/**
 * Created by guoyubo on 2017/10/16.
 */
public interface DtsUndo {

  List<String> buildRollbackSql();


  public static DtsUndo createDtsundo(RollbackInfor undoLog) {
    DtsUndo undo = null;
    switch (undoLog.getSqlType()) {
      case DELETE:
        undo = new DtsDeleteUndo(undoLog);
        break;
      case INSERT:
        undo = new DtsInsertUndo(undoLog);
        break;
      case UPDATE:
        undo = new DtsUpdateUndo(undoLog);
        break;
      default:
        throw new DtsException("sqltype error:" + undoLog.getSqlType());
    }

    return undo;
  }

}
