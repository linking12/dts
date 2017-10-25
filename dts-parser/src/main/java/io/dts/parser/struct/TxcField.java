package io.dts.parser.struct;

import com.alibaba.fastjson.JSON;

/**
 * 对数据库一个属性字段的描述
 * 
 * @author xiaoyan
 */
public class TxcField {
	/**
	 * 属性名
	 */
	private String name;

	/**
	 * 属性类型:java.sql.Types
	 */
	private int type;

	/**
	 * 属性值
	 */
	private Object value;

	/**
	 * 默认无参构造函数
	 */
	public TxcField() {
	}

	public String getFieldName() {
		return name;
	}

	public void setFieldName(String attrName) {
		this.name = attrName;
	}

	public int getFieldType() {
		return type;
	}

	public void setFieldType(int attrType) {
		this.type = attrType;
	}

	public Object getFieldValue() {
		return value;
	}

	public void setFieldValue(Object value) {
		this.value = jsonObjectSerialize(value);
	}

	public static Object jsonObjectSerialize(Object value) {
		if (value == null) {
			return null;
		} else if (java.util.Date.class.isAssignableFrom(value.getClass())) {
			return JSON.toJSONString(value);
		} else {
			return value;
		}
	}

	public boolean isKey(String pkname) {
		return name.equalsIgnoreCase(pkname);
	}
}
