package io.dts.parser.vistor.base;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;

import io.dts.parser.model.TxcTable;
import io.dts.parser.model.TxcTableMeta;
import io.dts.parser.vistor.support.ISQLStatement;

/**
 * @author xiaoyan
 */
public class TxcUpdateVisitor extends TxcBaseVisitor {


  private static final Logger logger = LoggerFactory.getLogger(TxcUpdateVisitor.class);


  public TxcUpdateVisitor(ISQLStatement node, List<Object> parameterSet) {
    super(node, parameterSet);
  }

  @Override
  public boolean visit(final MySqlUpdateStatement x) {
    setTableName(x.getTableName().toString());
    setTableNameAlias(x.getTableSource() != null ? x.getTableSource().getAlias() : null);

    return super.visit(x);
  }

  @Override
  public String parseWhereCondition(Statement st) {
    SQLUpdateStatement selectStatement = (SQLUpdateStatement) this.node.getSQLStatement();
    StringBuffer out = parseWhereCondition(selectStatement.getWhere());
    return out.toString();
  }

  @Override
  public TxcTable executeAndGetFrontImage(Statement st) throws SQLException {
    TxcTable tableOriginalValue;
    String sql = null;
    try {
      TxcTableMeta tableMeta = getTableMeta();
      tableOriginalValue = getTableOriginalValue();
      tableOriginalValue.setTableMeta(tableMeta);
      tableOriginalValue.setTableName(tableMeta.getTableName());
      tableOriginalValue.setAlias(tableMeta.getAlias());
      tableOriginalValue.setSchemaName(tableMeta.getSchemaName());
      sql = getSelectSql() + getWhereCondition(st) + " FOR UPDATE";
      tableOriginalValue.setLines(addLines(sql));
    } finally {
      logger.info("beforeSqlExecute:" + sql);
    }

    // if (tableOriginalValue.getLinesNum() == 0) {
    // throw new DtsException(2222, "null result for" + getInputSql());
    // }
    return tableOriginalValue;
  }

  @Override
  public TxcTable executeAndGetRearImage(Statement st) throws SQLException {
    TxcTable tableOriginalValue;
    TxcTable tablePresentValue;
    String sql = null;
    try {
      tableOriginalValue = getTableOriginalValue();
      tablePresentValue = getTablePresentValue();
      TxcTableMeta tableMeta = getTableMeta();

      // SQL执行后查询DB行现值，用户脏读检查
      // 更新后查询受影响行的现值
      // 构造以Key作为查询条件的select语句
      tablePresentValue.setTableMeta(tableMeta);
      tablePresentValue.setTableName(tableMeta.getTableName());
      tablePresentValue.setAlias(tableMeta.getAlias());
      tablePresentValue.setSchemaName(tableMeta.getSchemaName());
      sql = getSelectSql() + getWhereCondition(tableOriginalValue);
      tablePresentValue.setLines(addLines(sql));
    } finally {
      logger.info("afterSqlExecute:" + sql);
    }
    return tablePresentValue;
  }



}
