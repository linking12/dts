package io.dts.resourcemanager;

import javax.sql.DataSource;

import io.dts.common.cluster.ServerCluster;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by guoyubo on 2017/9/27.
 */
public class DataSourceHolder {

  private static ConcurrentHashMap<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

  public static void registerDataSource(String dbName, DataSource dataSource) {
    ServerCluster.getServerCluster();
    dataSourceMap.put(dbName, dataSource);
  }

  public static DataSource getDataSource(String dbName) {
    return dataSourceMap.get(dbName);
  }

}
