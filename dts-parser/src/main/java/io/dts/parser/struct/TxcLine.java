package io.dts.parser.struct;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

public class TxcLine {
	/**
	 * 保存与数据库表对应的一行内容
	 */
	private List<TxcField> fields = null;

	@JSONField(serialize = false)
	private TxcTableMeta tableMeta;

	public TxcTableMeta getTableMeta() {
		return tableMeta;
	}

	public void setTableMeta(TxcTableMeta tableMeta) {
		this.tableMeta = tableMeta;
	}

	public TxcLine() {
	}

	public List<TxcField> getFields() {
		return fields;
	}

	public void addFields(TxcField field) {
		if (fields == null) {
			fields = new ArrayList<TxcField>();
		}
		fields.add(field);
	}

	public void setFields(List<TxcField> fields) {
		this.fields = fields;
	}

	public int getFieldsNum() {
		return fields.size();
	}
}
