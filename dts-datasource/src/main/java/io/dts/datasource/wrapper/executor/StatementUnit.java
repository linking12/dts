
package io.dts.datasource.wrapper.executor;

import java.sql.SQLException;

import io.dts.parser.constant.SqlType;
import io.dts.parser.util.SqlTypeParser;
import io.dts.resourcemanager.api.IDtsDataSource;
import io.dts.resourcemanager.api.IDtsStatement;


public final class StatementUnit {

  private final SQLExecutionUnit sqlExecutionUnit;

  private final IDtsStatement statement;

  public StatementUnit(SQLExecutionUnit sqlExecutionUnit, IDtsStatement statement) {
    super();
    this.sqlExecutionUnit = sqlExecutionUnit;
    this.statement = statement;
  }

  public SQLExecutionUnit getSqlExecutionUnit() {
    return sqlExecutionUnit;
  }

  public IDtsStatement getStatement() {
    return statement;
  }

  public static class SQLExecutionUnit {
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

  }

}
