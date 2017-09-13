package org.dts.datasource;

import javax.sql.DataSource;

import java.sql.SQLException;

public interface IDtsDataSource extends DataSource {
	/**
	 * 获取不带事务的datasource
	 * 
	 * @return DataSource
	 * @throws SQLException
	 */
	DataSource getCommonDataSource() throws SQLException;

	/**
	 * 获取数据源名：unitName+appName+dbKey
	 * 
	 * @return
	 */
	String getDbName();

	String getInstanceId();
}
