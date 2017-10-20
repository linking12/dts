package io.dts.resourcemanager.api;

import java.sql.SQLException;
import java.sql.Statement;

public interface IDtsStatement extends Statement {

	IDtsConnection getDtsConnection() throws SQLException;

	Statement getRawStatement() throws SQLException;

	String getTargetSql();
}
