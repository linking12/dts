package io.dts.resourcemanager.undo;

import com.google.common.collect.Lists;

import java.util.List;

import io.dts.common.common.exception.DtsException;
import io.dts.common.protocol.ResultCode;
import io.dts.parser.model.RollbackInfor;
import io.dts.parser.model.TxcField;

/**
 * 
 * @author xiaoyan
 */
public class DtsUpdateUndo extends AbstractDtsUndo {

  public DtsUpdateUndo(RollbackInfor txcUndoLogRollbackInfor) {
    super(txcUndoLogRollbackInfor);
  }

  /**
   * 根据数据行原值还原数据库<br>
   * SQL如果修改了表关键字，会造成回滚失败<br>
   */
  @Override
  public List<String> buildRollbackSql() {
    // 检查脏写
    String rule = getTxcUndoLogRollbackInfor().getRollbackRule();
    if (rule == null) {
      // 检查数据行是否一致，update不应该影响行数目
      if (getOriginalValue().getLinesNum() != getPresentValue().getLinesNum()) {
        throw new DtsException(ResultCode.LOGICERROR.getValue(), "line num changed.");
      }
    }
    List<String> sqls = Lists.newArrayList();

    for (int index = 0; index < getOriginalValue().getLinesNum(); index++) {
      // 得到行的所有属性
      String tableName = getOriginalValue().getTableMeta().getTableName();
      String pkName = getOriginalValue().getTableMeta().getPkName();
      List<TxcField> fields = getOriginalValue().getLines().get(index).getFields();

      StringBuilder sqlAppender = new StringBuilder();
      sqlAppender.append("UPDATE ");
      sqlAppender.append(tableName);
      sqlAppender.append(" SET ");
      if (rule == null)
        sqlAppender.append(fieldsExpressionSerialization(fields, ",", pkName, false));
      else
        sqlAppender.append(rule);
      sqlAppender.append(" WHERE ");
      sqlAppender.append(fieldsExpressionSerialization(fields, "AND", pkName, true));
      sqls.add(sqlAppender.toString());
    }
    return sqls;
  }
}
