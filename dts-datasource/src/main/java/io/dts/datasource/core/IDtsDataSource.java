package io.dts.datasource.core;

import javax.sql.DataSource;

import java.sql.SQLException;

import io.dts.parser.constant.DatabaseType;
import io.dts.resourcemanager.executor.ResourceManager;

public interface IDtsDataSource extends DataSource {
	/**
	 * 获取不带事务的datasource
	 * 
	 * @return DataSource
	 * @throws SQLException
	 */
	DataSource getRawDataSource() throws SQLException;

	/**
	 * 获取数据源名：unitName+appName+dbKey
	 * 
	 * @return
	 */
	String getDbName();

	ResourceManager getResourceManager();

	DatabaseType getDatabaseType() throws SQLException;

}
