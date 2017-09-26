package io.dts.parser.vistor.support;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.List;

import io.dts.common.exception.DtsException;
import io.dts.parser.constant.SqlType;
import io.dts.parser.util.SqlTypeParser;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * Created by guoyubo on 2017/9/23.
 */
public class DtsSQLStatement implements ISQLStatement {

  private String sql;

  private SqlType sqlType;

  private Statement statement;

  public DtsSQLStatement(String sql) throws JSQLParserException, SQLException {
    this.sql = sql;
    CCJSqlParserManager parserManager = new CCJSqlParserManager();
    this.statement = parserManager.parse(new StringReader(sql));
    this.sqlType = SqlTypeParser.getSqlType(sql);
  }

  @Override
  public Statement getStatement() {
    return statement;
  }

  @Override
  public SqlType getType() {
    return sqlType;
  }

  @Override
  public String getSql() {
    return sql;
  }

  @Override
  public String getTableNameAlias() {
    switch (sqlType) {
      case SELECT:
        FromItem fromItem = ((PlainSelect) ((Select) getStatement()).getSelectBody()).getFromItem();
        if (!(fromItem instanceof Table)) {
          throw new DtsException("can't support complex sql");
        }
        return fromItem.getAlias() != null ? fromItem.getAlias().getName() : null;
      case UPDATE:
        List<Table> tables = ((Update) getStatement()).getTables();
        if (tables.size() > 1) {
          throw new DtsException("can't support multi table for update");
        }
        return tables.get(0).getAlias() != null ? tables.get(0).getAlias().getName() : null;
      case DELETE:
        Alias alias = ((Delete) getStatement()).getTable().getAlias();
        return  alias != null ? alias.getName() : null;
      case INSERT:
        Alias alias2 = ((Insert) getStatement()).getTable().getAlias();
        return  alias2 != null ? alias2.getName() : null;
      default:
        break;
    }

    return null;
  }


  @Override
  public String getTableName() {
    switch (sqlType) {
      case SELECT:
        FromItem fromItem = ((PlainSelect) ((Select) getStatement()).getSelectBody()).getFromItem();
        if (!(fromItem instanceof Table)) {
          throw new DtsException("can't support complex sql");
        }
        return  ((Table) fromItem).getName();
      case UPDATE:
        List<Table> tables = ((Update) getStatement()).getTables();
        if (tables.size() > 1) {
          throw new DtsException("can't support multi table for update");
        }
        return tables.get(0) != null ? tables.get(0).getName() : null;
      case DELETE:
        return  ((Delete) getStatement()).getTable().getName();
      case INSERT:
        Table table = ((Insert) getStatement()).getTable();
        return  table != null ? table.getName() : null;
      default:
        break;
    }

    return null;
  }
}
