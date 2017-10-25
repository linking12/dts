package io.dts.resourcemanager.undo;

import com.google.common.collect.Lists;

import java.util.List;

import io.dts.parser.model.RollbackInfor;
import io.dts.parser.model.TxcField;

/**
 * 
 * @author xiaoyan
 */
public class DtsDeleteUndo extends AbstractDtsUndo {

	public DtsDeleteUndo(RollbackInfor txcUndoLogRollbackInfor) {
		super(txcUndoLogRollbackInfor);
	}

	@Override
	public List<String> buildRollbackSql() {
		List<String> sqls = Lists.newArrayList();
		// 逐行反向写回数据库
		for (int index = 0; index < getOriginalValue().getLinesNum(); index++) {
			// 得到行的所有属性
			String tableName = getOriginalValue().getTableMeta().getTableName();
			List<TxcField> fields = getOriginalValue().getLines().get(index).getFields();
			StringBuilder sqlAppender = new StringBuilder();

			sqlAppender.append("INSERT INTO ");
			sqlAppender.append(tableName);
			sqlAppender.append("(");
			sqlAppender.append(fieldNamesSerialization(fields));
			sqlAppender.append(")");

			sqlAppender.append(" VALUES ");
			sqlAppender.append("(");
			sqlAppender.append(fieldsValueSerialization(fields));
			sqlAppender.append(")");
			sqls.add(sqlAppender.toString());
		}
		return sqls;
	}
}
