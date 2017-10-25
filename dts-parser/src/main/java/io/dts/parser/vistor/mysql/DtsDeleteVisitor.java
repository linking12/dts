package io.dts.parser.vistor.mysql;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;

import io.dts.parser.DtsSQLStatement;
import io.dts.parser.model.TxcTable;
import io.dts.parser.model.TxcTableMeta;


public class DtsDeleteVisitor extends AbstractDtsVisitor {

  public DtsDeleteVisitor(DtsSQLStatement node, List<Object> parameterSet) {
    super(node, parameterSet);
  }

  @Override
  public String parseWhereCondition(Statement st) {
    SQLDeleteStatement selectStatement = (SQLDeleteStatement) this.node.getSQLStatement();
    StringBuffer out = parseWhereCondition(selectStatement.getWhere());
    return out.toString();
  }

  @Override
  public TxcTable executeAndGetFrontImage(Statement st) throws SQLException {
    String sql = getSelectSql() + getWhereCondition(st) + " FOR UPDATE";

    TxcTable tableOriginalValue = getTableOriginalValue();
    TxcTableMeta tableMeta = getTableMeta();

    tableOriginalValue.setTableMeta(tableMeta);
    tableOriginalValue.setTableName(tableMeta.getTableName());
    tableOriginalValue.setAlias(tableMeta.getAlias());
    tableOriginalValue.setSchemaName(tableMeta.getSchemaName());
    tableOriginalValue.setLines(addLines(sql));
    return tableOriginalValue;
  }

  @Override
  public TxcTable executeAndGetRearImage(Statement st) throws SQLException {
    // SQL执行后查询DB行现值，用户脏读检查
    // 删除后，数据库已没有满足条件的数据，不需操作
    TxcTableMeta tableMeta = getTableMeta();
    TxcTable tablePresentValue = getTablePresentValue();
    tablePresentValue.setTableMeta(tableMeta);
    tablePresentValue.setTableName(tableMeta.getTableName());
    tablePresentValue.setAlias(tableMeta.getAlias());
    tablePresentValue.setSchemaName(tableMeta.getSchemaName());
    return tablePresentValue;
  }

  @Override
  public boolean visit(final MySqlDeleteStatement x) {
    setTableName(x.getTableName().toString());
    setTableNameAlias(x.getAlias() != null ? x.getAlias() : null);
    return super.visit(x);
  }

}
