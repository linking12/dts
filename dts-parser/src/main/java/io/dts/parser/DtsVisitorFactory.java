package io.dts.parser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.db2.parser.DB2StatementParser;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;

import io.dts.common.exception.DtsException;
import io.dts.parser.struct.DatabaseType;
import io.dts.parser.struct.SqlType;
import io.dts.parser.vistor.ITxcVisitor;
import io.dts.parser.vistor.SQLVisitorRegistry;

public class DtsVisitorFactory {

  /**
   * 获取SQL解析器<br>
   * 由各种数据源分别维护<br>
   *
   * @return
   * @throws SQLException
   */
  public static ITxcVisitor createSqlVisitor(final DatabaseType databaseType, Connection connection,
      final String sql, final List<Object> parameterSet) throws SQLException {
    SQLStatement sqlStatement = getSQLStatementParser(databaseType, sql).parseStatement();
    ITxcVisitor visit = getSQLVisitor(databaseType, parameterSet, sql, sqlStatement);
    visit.setConnection(connection);
    return visit;
  }


  private static SQLStatementParser getSQLStatementParser(final DatabaseType databaseType,
      final String sql) {
    switch (databaseType) {
      case H2:
      case MySQL:
        return new MySqlStatementParser(sql);
      case Oracle:
        return new OracleStatementParser(sql);
      case SQLServer:
        return new SQLServerStatementParser(sql);
      case DB2:
        return new DB2StatementParser(sql);
      default:
        throw new UnsupportedOperationException(
            String.format("Cannot support database type [%s]", databaseType));
    }
  }

  private static ITxcVisitor getSQLVisitor(final DatabaseType databaseType,
      final List<Object> parameterSet, final String sql, final SQLStatement sqlStatement) {
    if (sqlStatement instanceof SQLSelectStatement) {
      return VisitorLogProxy.enhance(SQLVisitorRegistry.getSelectVistor(databaseType),
          new DtsSQLStatement(sql, SqlType.SELECT, databaseType, sqlStatement), parameterSet);
    }
    if (sqlStatement instanceof SQLInsertStatement) {
      return VisitorLogProxy.enhance(SQLVisitorRegistry.getInsertVistor(databaseType),
          new DtsSQLStatement(sql, SqlType.INSERT, databaseType, sqlStatement), parameterSet);
    }
    if (sqlStatement instanceof SQLUpdateStatement) {
      return VisitorLogProxy.enhance(SQLVisitorRegistry.getUpdateVistor(databaseType),
          new DtsSQLStatement(sql, SqlType.UPDATE, databaseType, sqlStatement), parameterSet);
    }
    if (sqlStatement instanceof SQLDeleteStatement) {
      return VisitorLogProxy.enhance(SQLVisitorRegistry.getDeleteVistor(databaseType),
          new DtsSQLStatement(sql, SqlType.DELETE, databaseType, sqlStatement), parameterSet);
    }
    throw new DtsException("Unsupported SQL statement: " + sqlStatement);
  }


}
