package io.dts.datasource.statement;

import java.sql.SQLException;
import java.sql.Statement;

import io.dts.datasource.connection.IDtsConnection;

public interface IDtsStatement extends Statement {

	IDtsConnection getDtsConnection() throws SQLException;

	Statement getRawStatement() throws SQLException;

	String getTargetSql();
}
