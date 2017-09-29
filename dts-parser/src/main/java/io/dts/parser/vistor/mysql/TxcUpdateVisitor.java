package io.dts.parser.vistor.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import io.dts.common.exception.DtsException;
import io.dts.parser.hint.TxcHint;
import io.dts.parser.model.TxcTable;
import io.dts.parser.model.TxcTableMeta;
import io.dts.parser.vistor.support.ISQLStatement;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.Update;

/**
 * @author xiaoyan
 */
public class TxcUpdateVisitor extends TxcBaseVisitor {


	private static final Logger logger = LoggerFactory.getLogger(TxcUpdateVisitor.class);


	public TxcUpdateVisitor(Connection connection, ISQLStatement stmt) throws SQLException {
		super(connection, stmt);
	}

	private Update getSqlStatement() {
		return (Update) getSQLStatement().getStatement();
	}

	@Override
	public String parseWhereCondition(Statement st) {
		Update sqlStatement = getSqlStatement();
		StringBuilder appendable = new StringBuilder();
		Expression where = sqlStatement.getWhere();
		appendable.append(where.toString());
		return appendable.toString();
	}



	@Override
	public TxcTable executeAndGetFrontImage(Statement st) throws SQLException {
		String rule = TxcHint.getTxcRule(this.getInputSql());
		if (rule != null) {
			logger.info("rule:" + rule);
			setRollbackRule(rule);
		}

		TxcTable tableOriginalValue;
		String sql = null;
		try {
			TxcTableMeta tableMeta = getTableMeta();
			tableOriginalValue = getTableOriginalValue();
			tableOriginalValue.setTableMeta(tableMeta);
			tableOriginalValue.setTableName(tableMeta.getTableName());
			tableOriginalValue.setAlias(tableMeta.getAlias());
			tableOriginalValue.setSchemaName(tableMeta.getSchemaName());
			sql = getSelectSql() + getWhereCondition(st) + " FOR UPDATE";
			tableOriginalValue.setLines(addLines(sql));
		} finally {
			logger.info("beforeSqlExecute:" + sql);
		}

		if (tableOriginalValue.getLinesNum() == 0) {
			throw new DtsException(2222, "null result for" + getInputSql());
		}
		return tableOriginalValue;
	}

	@Override
	public TxcTable executeAndGetRearImage(Statement st) throws SQLException {
		if (getRollbackRule() != null) {
			return null;
		}

		TxcTable tableOriginalValue;
		TxcTable tablePresentValue;
		String sql = null;
		try {
			tableOriginalValue = getTableOriginalValue();
			tablePresentValue = getTablePresentValue();
			TxcTableMeta tableMeta = getTableMeta();

			// SQL执行后查询DB行现值，用户脏读检查
			// 更新后查询受影响行的现值
			// 构造以Key作为查询条件的select语句
			tablePresentValue.setTableMeta(tableMeta);
			tablePresentValue.setTableName(tableMeta.getTableName());
			tablePresentValue.setAlias(tableMeta.getAlias());
			tablePresentValue.setSchemaName(tableMeta.getSchemaName());
			sql = getSelectSql() + getWhereCondition(tableOriginalValue);
			tablePresentValue.setLines(addLines(sql));
		} finally {
			logger.info("afterSqlExecute:" + sql);
		}
		return tablePresentValue;
	}


	@Override
	public String getsql(String extraWhereCondition) {
		StringBuilder appendable = new StringBuilder();


		return appendable.toString();
	}


}
