package io.dts.datasource.executor;

import java.sql.SQLException;

import io.dts.parser.constant.SqlType;
import io.dts.parser.util.SqlTypeParser;
import io.dts.resourcemanager.api.IDtsDataSource;

/**
 * Created by guoyubo on 2017/9/21.
 */
public final class SQLExecutionUnit {



  private final IDtsDataSource dataSource;

  private final String sql;

  public SQLExecutionUnit(IDtsDataSource dataSource, String sql) {
    super();
    this.dataSource = dataSource;
    this.sql = sql;
  }


  public SqlType getSqlType() throws SQLException {
    return SqlTypeParser.getSqlType(sql);
  }


  public IDtsDataSource getDataSource() {
    return dataSource;
  }


  public String getSql() {
    return sql;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataSource == null) ? 0 : dataSource.hashCode());
    result = prime * result + ((sql == null) ? 0 : sql.hashCode());
    return result;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SQLExecutionUnit other = (SQLExecutionUnit) obj;
    if (dataSource == null) {
      if (other.dataSource != null)
        return false;
    } else if (!dataSource.equals(other.dataSource))
      return false;
    if (sql == null) {
      if (other.sql != null)
        return false;
    } else if (!sql.equals(other.sql))
      return false;
    return true;
  }


  @Override
  public String toString() {
    return "SQLExecutionUnit [dataSource=" + dataSource + ", sql=" + sql + "]";
  }


}

