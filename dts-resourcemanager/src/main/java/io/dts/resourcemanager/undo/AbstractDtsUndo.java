package io.dts.resourcemanager.undo;

import java.util.List;

import io.dts.parser.model.RollbackInfor;
import io.dts.parser.model.TxcField;
import io.dts.parser.model.TxcLine;
import io.dts.parser.model.TxcTable;
import io.dts.parser.vistor.support.TxcObjectWapper;

public abstract class AbstractDtsUndo implements DtsUndo {

  private final RollbackInfor txcUndoLogRollbackInfor;

  public AbstractDtsUndo(RollbackInfor txcUndoLogRollbackInfor) {
    this.txcUndoLogRollbackInfor = txcUndoLogRollbackInfor;
  }


  /**
   * 序列化表的属性<br>
   * like "field1,field2..."
   * 
   * @param fields
   * @return
   */
  protected String fieldNamesSerialization(List<TxcField> fields) {
    StringBuilder appender = new StringBuilder();
    boolean bAndFlag = true;
    for (int i = 0; i < fields.size(); i++) {
      TxcField field = fields.get(i);
      if (bAndFlag) {
        bAndFlag = false;
      } else {
        appender.append(", ");
      }
      appender.append('`');
      appender.append(field.getFieldName());
      appender.append('`');
    }

    return appender.toString();
  }

  protected String fieldsValueSerialization(List<TxcField> fields) {
    StringBuilder appender = new StringBuilder();
    boolean bStokFlag = true;
    for (int i = 0; i < fields.size(); i++) {
      TxcField field = fields.get(i);

      if (bStokFlag) {
        bStokFlag = false;
      } else {
        appender.append(", ");
      }

      appender.append(
          TxcObjectWapper.jsonObjectDeserialize(field.getFieldType(), field.getFieldValue()));
    }

    return appender.toString();
  }

  /**
   * 序列化表属性表达式 <br>
   * like: "name = value stok name = value ..."
   * 
   * @param fields
   * @param stok
   * @param onlyKey
   * @return
   */
  protected String fieldsExpressionSerialization(List<TxcField> fields, String stok, String pkname,
      boolean onlyKey) {
    StringBuilder appender = new StringBuilder();
    boolean bStokFlag = true;
    for (TxcField field : fields) {
      // 在只关注主键的情况下，非主键属性忽略
      if (onlyKey && field.isKey(pkname) == false) {
        continue;
      }

      if (bStokFlag) {
        bStokFlag = false;
      } else {
        appender.append(" " + stok + " ");
      }

      appender.append('`');
      appender.append(field.getFieldName());
      appender.append('`');
      appender.append(" = ");
      appender.append(
          TxcObjectWapper.jsonObjectDeserialize(field.getFieldType(), field.getFieldValue()));
    }
    return appender.toString();
  }

  /**
   * 获取所有行拼接的表达式
   * 
   * @param lines
   * @param onlyKey
   * @return
   */
  protected String linesExpressionSerialization(List<TxcLine> lines, String pkname,
      boolean onlyKey) {
    StringBuilder appender = new StringBuilder();
    boolean bOrFlag = true;
    for (int index = 0; index < lines.size(); index++) {
      TxcLine line = lines.get(index);

      if (bOrFlag) {
        bOrFlag = false;
      } else {
        appender.append(" OR ");
      }

      appender.append(fieldsExpressionSerialization(line.getFields(), "AND", pkname, onlyKey));
    }

    return appender.toString();
  }

  /**
   * SQL日志信息
   */
  public RollbackInfor getTxcUndoLogRollbackInfor() {
    return txcUndoLogRollbackInfor;
  }

  /**
   * 表修改前纪录
   */
  public TxcTable getOriginalValue() {
    return txcUndoLogRollbackInfor.getOriginalValue();
  }

  /**
   * 表修改后纪录
   */
  public TxcTable getPresentValue() {
    return txcUndoLogRollbackInfor.getPresentValue();
  }
}
