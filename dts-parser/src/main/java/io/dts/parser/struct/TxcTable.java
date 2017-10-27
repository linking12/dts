package io.dts.parser.struct;

import com.alibaba.fastjson.annotation.JSONField;

import io.dts.common.exception.DtsException;

import java.util.ArrayList;
import java.util.List;

public class TxcTable {
	@JSONField(serialize = false)
	private TxcTableMeta tableMeta;
	private String schemaName; // 实例名
	private String tableName; // 表名
	private String alias; // 别名
	private List<TxcLine> lines = new ArrayList<TxcLine>();

	public TxcTable() {
	}

	public int getLinesNum() {
		return lines.size();
	}

	public List<TxcLine> getLines() {
		return lines;
	}

	public void setLines(List<TxcLine> lineList) {
		this.lines = lineList;
	}

	public void addLine(TxcLine line) {
		lines.add(line);

		if (lines.size() > 1000) {
			throw new DtsException("one sql operated too much lines");
		}
	}

	@SuppressWarnings("serial")
	public List<TxcField> pkRows() {
		final String pkName = getTableMeta().getPkName();
		return new ArrayList<TxcField>() {
			{
				for (TxcLine line : lines) {
					List<TxcField> fields = line.getFields();
					for (TxcField field : fields) {
						if (field.getFieldName().equalsIgnoreCase(pkName)) {
							add(field);
							break;
						}
					}
				}
			}
		};
	}

	public TxcTableMeta getTableMeta() {
		if (tableMeta == null) {
			throw new DtsException("should set table meta when table data init");
		}
		return tableMeta;
	}

	public void setTableMeta(TxcTableMeta tableMeta) {
		this.tableMeta = tableMeta;
	}

	public String toString() {
		StringBuilder appender = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			List<TxcField> line = lines.get(i).getFields();
			for (TxcField obj : line) {
				appender.append(obj.getFieldValue());
			}
		}
		return appender.toString();
	}

	public String toStringWithEndl() {
		StringBuilder appender = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			List<TxcField> line = lines.get(i).getFields();
			for (TxcField obj : line) {
				appender.append(obj.getFieldValue()).append(":");
			}
			appender.append("\n");
		}
		return appender.toString();
	}

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

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}
