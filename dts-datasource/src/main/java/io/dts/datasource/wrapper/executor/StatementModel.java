
package io.dts.datasource.wrapper.executor;

import java.sql.SQLException;

import io.dts.parser.constant.SqlType;
import io.dts.parser.util.SqlTypeParser;
import io.dts.resourcemanager.api.IDtsDataSource;
import io.dts.resourcemanager.api.IDtsStatement;


public final class StatementModel {

  private final IDtsStatement statement;

  private final IDtsDataSource dataSource;

  private final String sql;

  public StatementModel(IDtsDataSource dataSource, IDtsStatement statement, String sql) {
    super();
    this.statement = statement;
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

  public IDtsStatement getStatement() {
    return statement;
  }
  
  

}
