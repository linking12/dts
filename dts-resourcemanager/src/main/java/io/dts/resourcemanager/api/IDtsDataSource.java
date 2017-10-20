package io.dts.resourcemanager.api;

import java.sql.SQLException;

import javax.sql.DataSource;

import io.dts.parser.constant.DatabaseType;

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


  DatabaseType getDatabaseType() throws SQLException;

}
