package io.dts.parser.struct;


import java.util.ArrayList;
import java.util.List;

public class TxcIndex {
	private List<TxcColumnMeta> values = new ArrayList<TxcColumnMeta>();

	private boolean nonUnique; // 不唯一
	private String indexQualifier;// 索引目录（可能为空）
	private String indexName;// 索引的名称
	private short type;// 索引类型
	private IndexType indextype;
	private String ascOrDesc;// 列排序顺序:升序还是降序
	private int cardinality; // 基数
	private int ordinalPosition; // 表中列的索引（从1开始）

	public TxcIndex() {
	}

//	public TxcIndex(ColumnMeta column) {
//		indexName = column.getName();
//		values.add(new TxcColumnMeta(column));
//	}

	public List<TxcColumnMeta> getValues() {
		return values;
	}

	public void setValues(List<TxcColumnMeta> values) {
		this.values = values;
	}

	public boolean isNonUnique() {
		return nonUnique;
	}

	public void setNonUnique(boolean nonUnique) {
		this.nonUnique = nonUnique;
	}

	public String getIndexQualifier() {
		return indexQualifier;
	}

	public void setIndexQualifier(String indexQualifier) {
		this.indexQualifier = indexQualifier;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public String getAscOrDesc() {
		return ascOrDesc;
	}

	public void setAscOrDesc(String ascOrDesc) {
		this.ascOrDesc = ascOrDesc;
	}

	public int getCardinality() {
		return cardinality;
	}

	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}

	public int getOrdinalPosition() {
		return ordinalPosition;
	}

	public void setOrdinalPosition(int ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}

	public IndexType getIndextype() {
		return indextype;
	}

	public void setIndextype(IndexType indextype) {
		this.indextype = indextype;
	}

	public List<TxcColumnMeta> getIndexvalue() {
		return values;
	}

	public String toString() {
		return "indexName:" + indexName + "->" + "type:" + type + "->" + "values:" + values;
	}
}
