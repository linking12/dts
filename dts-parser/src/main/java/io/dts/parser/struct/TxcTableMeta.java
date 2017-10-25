package io.dts.parser.struct;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TxcTableMeta {

	private String schemaName; // 实例名
	private String tableName; // 表名
	private String alias; // 别名

	private Map<String/* 属性名 */, TxcColumnMeta> allColumns = new HashMap<String, TxcColumnMeta>();
	private Map<String/* 索引名 */, TxcIndex> allIndexes = new HashMap<String, TxcIndex>();

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * 获得表得某一个属性
	 */
	public TxcColumnMeta getColumnMeta(String colName) {
		return allColumns.get(colName);
	}

	/**
	 * 获取所有列
	 */
	public Map<String, TxcColumnMeta> getAllColumns() {
		return allColumns;
	}

	/**
	 * 获取表索引信息
	 */
	public Map<String, TxcIndex> getAllIndexes() {
		return allIndexes;
	}

	/**
	 * 获取自增主键列
	 */
	public TxcColumnMeta getAutoIncreaseColumn() {
		for (Entry<String, TxcColumnMeta> entry : allColumns.entrySet()) {
			TxcColumnMeta col = entry.getValue();
			if ("YES".equalsIgnoreCase(col.getIsAutoincrement()) == true) {
				return col;
			}
		}
		return null;
	}

	/**
	 * 获取表主键列
	 */
	public Map<String, TxcColumnMeta> getPrimaryKeyMap() {
		Map<String, TxcColumnMeta> pk = new HashMap<String, TxcColumnMeta>();
		for (Entry<String, TxcIndex> entry : allIndexes.entrySet()) {
			TxcIndex index = entry.getValue();
			if (index.getIndextype().value() == IndexType.PRIMARY.value()) {
				for (TxcColumnMeta col : index.getValues()) {
					pk.put(col.getColumnName().toUpperCase(), col);
				}
			}
		}

		if (pk.size() > 1) {
			throw new RuntimeException("mutil pks not support yet.");
		}

		return pk;
	}

	/**
	 * 获取主键列名
	 */
	@SuppressWarnings("serial")
	public List<String> getPrimaryKeyOnlyName() {
		return new ArrayList<String>() {
			{
				for (Entry<String, TxcColumnMeta> entry : getPrimaryKeyMap().entrySet()) {
					add(entry.getKey());
				}
			}
		};
	}

	public String getPkName() {
		return getPrimaryKeyOnlyName().get(0);
	}

	/**
	 * 判断输入列中是否包含了主键列
	 */
	public boolean isContainsPriKey(List<String> cols) {
		if (cols == null) {
			return false;
		}

		List<String> pk = getPrimaryKeyOnlyName();
		if (pk.isEmpty()) {
			return false;
		}

		return cols.containsAll(pk);
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getCreateTableSQL() {
		StringBuilder sb = new StringBuilder("CREATE TABLE");
		sb.append(String.format(" `%s` ", getTableName()));
		sb.append("(");

		boolean flag = true;
		Map<String, TxcColumnMeta> allColumns = getAllColumns();
		for (Entry<String, TxcColumnMeta> entry : allColumns.entrySet()) {
			if (flag == true) {
				flag = false;
			} else {
				sb.append(",");
			}

			TxcColumnMeta col = entry.getValue();
			sb.append(String.format(" `%s` ", col.getColumnName()));
			sb.append(col.getDataTypeName());
			if (col.getColumnSize() > 0) {
				sb.append(String.format("(%d)", col.getColumnSize()));
			}

			if (col.getColumnDef() != null && col.getColumnDef().length() > 0) {
				sb.append(String.format(" default '%s'", col.getColumnDef()));
			}

			if (col.getIsNullAble() != null && col.getIsNullAble().length() > 0) {
				sb.append(" ");
				sb.append(col.getIsNullAble());
			}
		}

		Map<String, TxcIndex> allIndexes = getAllIndexes();
		for (Entry<String, TxcIndex> entry : allIndexes.entrySet()) {
			sb.append(", ");

			TxcIndex index = entry.getValue();
			switch (index.getIndextype()) {
			case FullText:
				break;
			case Normal:
				sb.append(String.format("KEY `%s`", index.getIndexName()));
				break;
			case PRIMARY:
				sb.append(String.format("PRIMARY KEY"));
				break;
			case Unique:
				sb.append(String.format("UNIQUE KEY `%s`", index.getIndexName()));
				break;
			default:
				break;
			}

			sb.append(" (");
			boolean f = true;
			for (TxcColumnMeta c : index.getValues()) {
				if (f == true) {
					f = false;
				} else {
					sb.append(",");
				}

				sb.append(String.format("`%s`", c.getColumnName()));
			}
			sb.append(")");
		}
		sb.append(")");

		return sb.toString();
	}
}
