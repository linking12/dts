package io.dts.datasource.statement;

import java.sql.SQLException;
import java.sql.Statement;

import io.dts.datasource.connection.ITxcConnection;

public interface ITxcStatement extends Statement {

	ITxcConnection getTxcConnection() throws SQLException;

	Statement getRawStatement() throws SQLException;

	String getTargetSql();
}
