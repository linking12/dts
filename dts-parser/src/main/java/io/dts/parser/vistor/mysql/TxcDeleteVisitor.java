package io.dts.parser.vistor.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import io.dts.common.exception.DtsException;
import io.dts.parser.model.TxcColumnMeta;
import io.dts.parser.model.TxcTable;
import io.dts.parser.model.TxcTableMeta;
import io.dts.parser.vistor.support.ISQLStatement;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;

/**
 * @author xiaoyan
 */
public class TxcDeleteVisitor extends TxcBaseVisitor {


	private static final Logger logger = LoggerFactory.getLogger(TxcDeleteVisitor.class);


	public TxcDeleteVisitor(Connection connection, ISQLStatement stmt) throws SQLException {
		super(connection, stmt);
	}


	@Override
	public String parseSelectSql() {
		StringBuilder selectSqlAppender = getNullSqlAppenderBuilder();
		selectSqlAppender.append("SELECT ");
		appendable.append(printColumns());
		selectSqlAppender.append(" FROM ");
		Delete sqlStatement = getSqlStatement();
		if (sqlStatement.getTables().size() > 1) {
			throw  new DtsException("support only one table");
		}
		Table table = sqlStatement.getTables().get(0);
		selectSqlAppender.append(table.getName());

		return selectSqlAppender.toString();
	}


	public String printColumns() {
		StringBuilder appender = new StringBuilder();

		Collection<TxcColumnMeta> list = (Collection<TxcColumnMeta>) getTableMeta().getAllColumns().values();

		boolean isFst = true;
		for (Object obj : list) {
			if (isFst) {
				isFst = false;
			} else if (obj instanceof TxcColumnMeta) {
				appender.append(",");
			}
			appender.append(getTableName());
			appender.append(".");

			appender.append(((TxcColumnMeta) obj).getColumnName());
		}

		return appender.toString();
	}

	private Delete getSqlStatement() {
		return (Delete) getSQLStatement().getStatement();
	}


	@Override
	public String parseWhereCondition(Statement st) {
		Delete sqlStatement = getSqlStatement();
		StringBuilder appendable = new StringBuilder();

		appendable.append(sqlStatement.getWhere().toString());

		return appendable.toString();
	}

	@Override
	public TxcTable executeAndGetFrontImage(Statement st) throws SQLException {
		String sql = getSelectSql() + getWhereCondition(st) + " FOR UPDATE";

		TxcTable tableOriginalValue = getTableOriginalValue();
		TxcTableMeta tableMeta = getTableMeta();

		tableOriginalValue.setTableMeta(tableMeta);
		tableOriginalValue.setTableName(tableMeta.getTableName());
		tableOriginalValue.setAlias(tableMeta.getAlias());
		tableOriginalValue.setSchemaName(tableMeta.getSchemaName());
		tableOriginalValue.setLines(addLines(sql));
		logger.info("executeAndGetFrontImage:" + sql);
		return tableOriginalValue;
	}

	@Override
	public TxcTable executeAndGetRearImage(Statement st) throws SQLException {
		// SQL执行后查询DB行现值，用户脏读检查
		// 删除后，数据库已没有满足条件的数据，不需操作
		TxcTableMeta tableMeta = getTableMeta();
		TxcTable tablePresentValue = getTablePresentValue();
		tablePresentValue.setTableMeta(tableMeta);
		tablePresentValue.setTableName(tableMeta.getTableName());
		tablePresentValue.setAlias(tableMeta.getAlias());
		tablePresentValue.setSchemaName(tableMeta.getSchemaName());
		return tablePresentValue;
	}


	@Override
	public String getsql(final String extraWhereCondition) {
		return null;
	}

}
