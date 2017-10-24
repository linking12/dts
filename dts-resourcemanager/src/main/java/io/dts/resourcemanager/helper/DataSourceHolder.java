package io.dts.resourcemanager.helper;

import javax.sql.DataSource;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by guoyubo on 2017/9/27.
 */
public class DataSourceHolder {

  private static ConcurrentHashMap<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

  public static void registerDataSource(String dbName, DataSource dataSource) {
    dataSourceMap.put(dbName, dataSource);
  }

  public static DataSource getDataSource(String dbName) {
    return dataSourceMap.get(dbName);
  }

}
