package io.dts.parser.vistor.support;

import java.io.StringReader;
import java.sql.SQLException;

import io.dts.common.exception.DtsException;
import io.dts.parser.constant.SqlType;
import io.dts.parser.util.SqlTypeParser;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
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
          throw new DtsException("support complex sql");
        }
        return fromItem.getAlias() != null ? fromItem.getAlias().getName() : null;
      case UPDATE:
        FromItem fromItem1 = ((Update) getStatement()).getFromItem();
        if (!(fromItem1 instanceof Table)) {
          throw new DtsException("support complex sql");
        }
        return fromItem1.getAlias() != null ? fromItem1.getAlias().getName() : null;
      case DELETE:
        Alias alias = ((Delete) getStatement()).getTable().getAlias();
        return  alias != null ? alias.getName() : null;
      case INSERT:
        break;
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
          throw new DtsException("support complex sql");
        }
        return  ((Table) fromItem).getName();
      case UPDATE:
        FromItem fromItem1 = ((Update) getStatement()).getFromItem();
        if (!(fromItem1 instanceof Table)) {
          throw new DtsException("support complex sql");
        }
        return ((Table) fromItem1).getName();
      case DELETE:
        return  ((Delete) getStatement()).getTable().getName();
      case INSERT:
        break;
      default:
        break;
    }

    return null;
  }
}
