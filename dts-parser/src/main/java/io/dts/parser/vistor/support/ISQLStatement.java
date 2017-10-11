package io.dts.parser.vistor.support;


import com.alibaba.druid.sql.ast.SQLStatement;

import io.dts.parser.constant.DatabaseType;
import io.dts.parser.constant.SqlType;

public interface ISQLStatement {


	SQLStatement getSQLStatement();

	SqlType getType();

	DatabaseType getDatabaseType();

	String getSql();

}
