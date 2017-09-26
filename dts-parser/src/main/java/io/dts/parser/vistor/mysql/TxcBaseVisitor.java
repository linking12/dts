package io.dts.parser.vistor.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.dts.common.exception.DtsException;
import io.dts.parser.constant.SqlType;
import io.dts.parser.model.TxcColumnMeta;
import io.dts.parser.model.TxcField;
import io.dts.parser.model.TxcLine;
import io.dts.parser.model.TxcTable;
import io.dts.parser.model.TxcTableMeta;
import io.dts.parser.vistor.ITxcVisitor;
import io.dts.parser.vistor.support.ISQLStatement;
import io.dts.parser.vistor.support.PlaceHolderManager;
import io.dts.parser.vistor.support.TxcTableMetaTools;

/**
 * 
 * @author xiaoyan
 */
public abstract class TxcBaseVisitor implements ITxcVisitor {
	private String selectSql = null;
	private String whereCondition = null;

	private TxcTableMeta tableMeta = null;// table语法树
	private PlaceHolderManager pm = null;
	private String rollbackRule = null; // 用户通过hint定义sql的自定义规则

	private ISQLStatement node;
	private final TxcTable tableOriginalValue = new TxcTable(); // 保存SQL前置镜像
	private final TxcTable tablePresentValue = new TxcTable(); // 保存SQL后置镜像
	protected StringBuilder appendable = new StringBuilder();

	protected Connection connection;

	protected String sql;

	public TxcBaseVisitor(final Connection connection, ISQLStatement stmt) throws SQLException {
		this.connection = connection;
		this.node = stmt;
		this.tableMeta = buildTableMeta();
	}


	public  TxcTableMeta buildTableMeta() throws SQLException {
		TxcTableMeta tableMeta = null;
		try {
			String tablename = node.getTableName();

			String tablenameAlias = node.getTableNameAlias();
			tableMeta = TxcTableMetaTools.getTableMeta(connection, tablename);
			tableMeta.setAlias(tablenameAlias);
		} catch (Exception e) {
			throw new DtsException(e, "getTableMeta error");
		}

		return tableMeta;
	}

	@Override
	public TxcTable getTableOriginalValue() {
		return tableOriginalValue;
	}

	@Override
	public TxcTable getTablePresentValue() {
		return tablePresentValue;
	}

	@Override
	public String getRollbackRule() {
		return rollbackRule;
	}

	public void setRollbackRule(String rollbackRule) {
		this.rollbackRule = rollbackRule;
	}

	@Override
	public TxcTableMeta getTableMeta() {
		return tableMeta;
	}

	@Override
	public String getSelectSql() {
		if (selectSql == null) {
			selectSql = parseSelectSql();
		}
		return selectSql;
	}

	protected abstract String parseSelectSql();

	@Override
	public String getWhereCondition(Statement st) {
		if (whereCondition == null) {
			whereCondition = parseWhereCondition(st);
		}
		return whereCondition;
	}

	@Override
	public String getWhereCondition(TxcTable table) {
		StringBuilder appender = new StringBuilder();

		Map<String, TxcColumnMeta> tableKeys = getTableMeta().getPrimaryKeyMap();
		if (tableKeys.size() <= 0) {
			throw new DtsException("table[" + getTableMeta().getTableName() + "] should has prikey, contact DBA please.");
		}

		List<TxcLine> lines = table.getLines();
		if (lines.size() > 0) {
			appender.append(" WHERE ");
		}

		boolean bOrFlag = true;
		for (int i = 0; i < lines.size(); i++) {
			TxcLine line = lines.get(i);
			List<TxcField> fields = line.getFields();
			if (fields == null) {
				continue;
			}

			if (bOrFlag) {
				bOrFlag = false;
			} else {
				appender.append(" OR ");
			}

			printKeyList(tableKeys, fields, appender);
		}

		return appender.toString();
	}

	void printKeyList(Map<String, TxcColumnMeta> tableKeys, List<TxcField> fields, StringBuilder appender) {
		boolean bAndFlag = true;
		for (int i = 0; i < fields.size(); i++) {
			TxcField field = fields.get(i);
			if (tableKeys.containsKey(field.getFieldName().toUpperCase())) {
				if (bAndFlag) {
					bAndFlag = false;
				} else {
					appender.append(" AND ");
				}
				appender.append(field.getFieldName());
				appender.append("=");
				TxcObjectWapper.appendParamMarkerObject(field.getFieldValue(), appender);
			}
		}
	}

	protected abstract String parseWhereCondition(final Statement st);

	@Override
	public SqlType getSqlType() {
		return node.getType();
	}

	@Override
	public ISQLStatement getSQLStatement() {
		return this.node;
	}

	@Override
	public String getInputSql() {
		return node.getSql();
	}

	@Override
	public void setPlaceHolderManager(PlaceHolderManager pm) {
		this.pm = pm;
	}

	@Override
	public PlaceHolderManager getPlaceHolderManager() {
		return pm;
	}

	@Override
	public String getTableName() {
		return getTableMeta().getTableName().toUpperCase();
	}

	public StringBuilder getSqlAppenderBuilder() {
		return appendable;
	}

	public StringBuilder getNullSqlAppenderBuilder() {
		StringBuilder appender = getSqlAppenderBuilder();
		appender.delete(0, appender.length());
		return appender;
	}

	private TxcLine addLine(ResultSet rs, ResultSetMetaData rsmd, int column) throws SQLException {
		List<TxcField> fields = new ArrayList<TxcField>(column);
		for (int i = 1; i <= column; i++) {
			TxcField field = new TxcField();
			field.setFieldName(rsmd.getColumnName(i));
			field.setFieldType(rsmd.getColumnType(i));
			field.setFieldValue(rs.getObject(i));
			fields.add(field);
		}

		TxcLine line = new TxcLine();
		line.setTableMeta(getTableMeta());
		line.setFields(fields);
		return line;
	}

	public List<TxcLine> addLines(String sql) throws SQLException {
		List<TxcLine> txcLines = new ArrayList<>();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = connection.createStatement();
			rs = st.executeQuery(sql);
			java.sql.ResultSetMetaData rsmd = rs.getMetaData();
			int column = rsmd.getColumnCount();

			while (rs.next()) {
				txcLines.add(addLine(rs, rsmd, column));
			}
			return txcLines;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				st.close();
			}
		}
	}

	
}
