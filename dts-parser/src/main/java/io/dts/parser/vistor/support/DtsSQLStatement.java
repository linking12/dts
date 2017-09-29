package io.dts.parser.vistor.support;

import com.alibaba.druid.sql.ast.SQLStatement;

import io.dts.parser.constant.DatabaseType;
import io.dts.parser.constant.SqlType;

/**
 * Created by guoyubo on 2017/9/23.
 */
public class DtsSQLStatement implements ISQLStatement {

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

  @Override
  public SQLStatement getSQLStatement() {
    return sqlStatement;
  }

  @Override
  public SqlType getType() {
    return sqlType;
  }

  @Override
  public DatabaseType getDatabaseType() {
    return databaseType;
  }

  @Override
  public String getSql() {
    return sql;
  }


}
