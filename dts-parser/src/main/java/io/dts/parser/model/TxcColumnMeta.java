package io.dts.parser.model;

import io.dts.parser.dialect.MySQLKeyword;

public class TxcColumnMeta {
	private String tableCat;// 表目录（可能为空）
	private String tableSchemaName;// 表的架构（可能为空）
	private String tableName_;// 表名
	private String columnName;// 列名
	private int dataType; // 对应的java.sql.Types类型
	private String dataTypeName;// java.sql.Types类型名称
	private int columnSize;// 列大小
	private int decimalDigits;// 小数位数
	private int numPrecRadix;// 基数（通常是10或2）
	private int nullAble;// 是否允许为null
	private String remarks;// 列描述
	private String columnDef;// 默认值
	private int sqlDataType;// sql数据类型
	private int sqlDatetimeSub; // SQL日期时间分?
	private int charOctetLength; // char类型的列中的最大字节数
	private int ordinalPosition; // 表中列的索引（从1开始）

	/**
	 * ISO规则用来确定某一列的为空性。 <br>
	 * 是---如果该参数可以包括空值; <br>
	 * 无---如果参数不能包含空值 空字符串---如果参数为空性是未知的<br>
	 */
	private String isNullAble;

	/**
	 * 指示此列是否是自动递增 <br>
	 * 是---如果该列是自动递增 <br>
	 * 无---如果不是自动递增列 <br>
	 * 空字串---如果不能确定它是否 列是自动递增的参数是未知<br>
	 */
	private String isAutoincrement;

	public TxcColumnMeta() {
	}

//	public TxcColumnMeta(ColumnMeta meta) {
//		setTableName(meta.getTableName());
//		setColumnName(meta.getName());
//		setDataType(meta.getField().getDataType().getSqlType());
//	}
	
	/**
	 * 是否为常规类型，非常规类型不会作为where条件参与查询
	 * 
	 * @return
	 */
	public boolean isCommonType() {
		boolean ret = false;

		int type = getDataType();
		switch (type) {
		case java.sql.Types.ARRAY:
		case java.sql.Types.BIGINT:
		case java.sql.Types.BOOLEAN:
		case java.sql.Types.CHAR:
		case java.sql.Types.DOUBLE:
		case java.sql.Types.FLOAT:
		case java.sql.Types.INTEGER:
		case java.sql.Types.LONGNVARCHAR:
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.NCHAR:
		case java.sql.Types.REAL:
		case java.sql.Types.TINYINT:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.NUMERIC:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.SMALLINT:
			ret = true;
			break;

		case java.sql.Types.BINARY:
		case java.sql.Types.BIT:
		case java.sql.Types.BLOB:
		case java.sql.Types.CLOB:
		case java.sql.Types.DATALINK:
		case java.sql.Types.DATE:
		case java.sql.Types.DECIMAL:
		case java.sql.Types.DISTINCT:
		case java.sql.Types.JAVA_OBJECT:
		case java.sql.Types.LONGVARBINARY:
		case java.sql.Types.NCLOB:
		case java.sql.Types.NULL:
		case java.sql.Types.OTHER:
		case java.sql.Types.REF:
		case java.sql.Types.ROWID:
		case java.sql.Types.SQLXML:
		case java.sql.Types.STRUCT:
		case java.sql.Types.TIME:
		case java.sql.Types.TIMESTAMP:
		case java.sql.Types.VARBINARY:
			break;
		default:
			throw new UnsupportedOperationException("unknown type:" + type);
		}

		return ret;
	}

	public String getTableCat() {
		return tableCat;
	}

	public void setTableCat(String tableCat) {
		this.tableCat = tableCat;
	}

	public String getTableSchemaName() {
		return tableSchemaName;
	}

	public void setTableSchemaName(String tableSchemaName) {
		this.tableSchemaName = tableSchemaName;
	}

	public String getTableName() {
		return tableName_;
	}

	public void setTableName(String tableName_) {
		this.tableName_ = tableName_;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		if (MySQLKeyword.isKeyword(columnName)) {
			StringBuilder sb = new StringBuilder(2 + columnName.length());
			sb.append("`");
			sb.append(columnName);
			sb.append("`");
			this.columnName = sb.toString();
		} else {
			this.columnName = columnName;
		}
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public String getDataTypeName() {
		return dataTypeName;
	}

	public void setDataTypeName(String dataTypeName) {
		this.dataTypeName = dataTypeName;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}

	public int getDecimalDigits() {
		return decimalDigits;
	}

	public void setDecimalDigits(int decimalDigits) {
		this.decimalDigits = decimalDigits;
	}

	public int getNumPrecRadix() {
		return numPrecRadix;
	}

	public void setNumPrecRadix(int numPrecRadix) {
		this.numPrecRadix = numPrecRadix;
	}

	public int getNullAble() {
		return nullAble;
	}

	public void setNullAble(int nullAble) {
		this.nullAble = nullAble;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getColumnDef() {
		return columnDef;
	}

	public void setColumnDef(String columnDef) {
		this.columnDef = columnDef;
	}

	public int getSqlDataType() {
		return sqlDataType;
	}

	public void setSqlDataType(int sqlDataType) {
		this.sqlDataType = sqlDataType;
	}

	public int getSqlDatetimeSub() {
		return sqlDatetimeSub;
	}

	public void setSqlDatetimeSub(int sqlDatetimeSub) {
		this.sqlDatetimeSub = sqlDatetimeSub;
	}

	public int getCharOctetLength() {
		return charOctetLength;
	}

	public void setCharOctetLength(int charOctetLength) {
		this.charOctetLength = charOctetLength;
	}

	public int getOrdinalPosition() {
		return ordinalPosition;
	}

	public void setOrdinalPosition(int ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}

	public String getIsNullAble() {
		return isNullAble;
	}

	public void setIsNullAble(String isNullAble) {
		this.isNullAble = isNullAble;
	}

	public String getIsAutoincrement() {
		return isAutoincrement;
	}

	public void setIsAutoincrement(String isAutoincrement) {
		this.isAutoincrement = isAutoincrement;
	}
}
