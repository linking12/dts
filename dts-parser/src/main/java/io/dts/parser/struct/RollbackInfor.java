package io.dts.parser.struct;

import java.io.Serializable;

public class RollbackInfor implements Serializable, Cloneable {

	private static final long serialVersionUID = -3175640556434929851L;

	/**
	 * 保存表原始值，用户构造回滚SQL
	 */
	private TxcTable originalValue = new TxcTable();

	/**
	 * 保存表现值，用户检查脏读
	 */
	private TxcTable presentValue = new TxcTable();

	/**
	 * 查询SQL，where之前的内容
	 */
	private String selectSql = "";

	/**
	 * 以表的关键字为查询条件
	 */
	private String whereCondition = "";

	/**
	 * SQL类型
	 */
	private SqlType sqlType = null;

	/**
	 * 原始SQL
	 */
	private String sql = "";

	/**
	 * 用户通过hint定义sql的自定义规则
	 */
	private String rollbackRule = null;

	/**
	 * 构造函数
	 */
	public RollbackInfor() {
	}

	/**
	 * 格式化输出
	 */
	public String toString() {
		StringBuilder strLog = new StringBuilder();
		strLog.append("\n");
		strLog.append("tableName:").append(originalValue.getTableMeta().getTableName()).append("\n");
		strLog.append("sqlType:").append(sqlType).append("\n");
		strLog.append("selectSql:").append(selectSql).append("\n");
		strLog.append("sql:").append(sql).append("\n");

		strLog.append("where:").append(whereCondition).append("\n");
		strLog.append("------------originalValue--------------[").append(originalValue.getLinesNum()).append("]\n");
		strLog.append(originalValue.toStringWithEndl());
		strLog.append("------------presentValue---------------[").append(presentValue.getLinesNum()).append("]\n");
		strLog.append(presentValue.toStringWithEndl());
		strLog.append("length:").append(strLog.length());

		return strLog.toString();
	}

	public boolean txcLogChecker() {
		boolean bRet = true;

		if (this.getSqlType() == SqlType.UPDATE) {
			if (this.getRollbackRule() != null) {
				return true;
			}

			TxcTable originalValue = this.getOriginalValue();
			TxcTable presentValue = this.getPresentValue();

			if (originalValue.getLinesNum() != presentValue.getLinesNum()) {
				throw new RuntimeException(SqlType.UPDATE + ":update line nums changed after sql excute, perhaps key column has changed." + this.toString());
			}
		}

		return bRet;
	}

	public TxcTable getOriginalValue() {
		return originalValue;
	}

	public void setOriginalValue(TxcTable originalValue) {
		this.originalValue = originalValue;
	}

	public String getRollbackRule() {
		return rollbackRule;
	}

	public void setRollbackRule(String rollbackRule) {
		this.rollbackRule = rollbackRule;
	}

	public TxcTable getPresentValue() {
		return presentValue;
	}

	public void setPresentValue(TxcTable presentValue) {
		this.presentValue = presentValue;
	}

	public String getSelectSql() {
		return selectSql;
	}

	public void setSelectSql(String sql) {
		this.selectSql = sql;
	}

	public String getWhereCondition() {
		return whereCondition;
	}

	public void setWhereCondition(String where) {
		this.whereCondition = where;
	}

	public SqlType getSqlType() {
		return this.sqlType;
	}

	public void setSqlType(SqlType sqlType) {
		this.sqlType = sqlType;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
}
