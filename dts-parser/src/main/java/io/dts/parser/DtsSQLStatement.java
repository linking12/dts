package io.dts.parser;

import com.alibaba.druid.sql.ast.SQLStatement;

import io.dts.parser.struct.DatabaseType;
import io.dts.parser.struct.SqlType;

/**
 * Created by guoyubo on 2017/9/23.
 */
public class DtsSQLStatement {

  private String sql;

  private SqlType sqlType;

  private DatabaseType databaseType;

  private SQLStatement sqlStatement;

  public DtsSQLStatement(final String sql, final SqlType sqlType, final DatabaseType databaseType,
      final SQLStatement sqlStatement) {
    this.sql = sql;
    this.sqlType = sqlType;
    this.databaseType = databaseType;
    this.sqlStatement = sqlStatement;
  }

  public SQLStatement getSQLStatement() {
    return sqlStatement;
  }

  public SqlType getType() {
    return sqlType;
  }

  public DatabaseType getDatabaseType() {
    return databaseType;
  }

  public String getSql() {
    return sql;
  }


}
