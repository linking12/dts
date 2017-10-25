package io.dts.parser.vistor.mysql;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;

import io.dts.parser.DtsSQLStatement;
import io.dts.parser.model.TxcTable;

public class DtsSelectVisitor extends AbstractDtsVisitor {

  public DtsSelectVisitor(DtsSQLStatement node, List<Object> parameterSet) {
    super(node, parameterSet);
  }

  @Override
  public String parseWhereCondition(Statement st) {
    SQLSelectStatement selectStatement = (SQLSelectStatement) this.node.getSQLStatement();
    StringBuffer out = parseWhereCondition(
        ((SQLSelectQueryBlock) selectStatement.getSelect().getQuery()).getWhere());
    return out.toString();
  }


  @Override
  public TxcTable executeAndGetFrontImage(final Statement st) throws SQLException {
    return getTableOriginalValue();
  }

  @Override
  public TxcTable executeAndGetRearImage(final Statement st) throws SQLException {
    return getTablePresentValue();
  }

  @Override
  public boolean visit(final MySqlSelectQueryBlock x) {
    if (x.getFrom() instanceof SQLExprTableSource) {
      SQLExprTableSource tableExpr = (SQLExprTableSource) x.getFrom();
      setTableName(tableExpr.getExpr().toString());
      setTableNameAlias(tableExpr.getAlias() != null ? tableExpr.getAlias() : null);
    }
    return super.visit(x);
  }

  public boolean visit(final SQLSelectItem x) {
    return super.visit(x);
  }


}
