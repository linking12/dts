package io.dts.parser.vistor.support;


import io.dts.parser.constant.SqlType;
import net.sf.jsqlparser.statement.Statement;

public interface ISQLStatement {

	Statement getStatement();

	SqlType getType();

	String getSql();

	String getTableNameAlias();

	String getTableName();
}
