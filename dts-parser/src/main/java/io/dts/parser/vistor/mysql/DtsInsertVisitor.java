package io.dts.parser.vistor.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlEvalVisitorImpl;
import com.alibaba.druid.sql.visitor.SQLEvalVisitor;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils;
import com.alibaba.druid.util.JdbcUtils;
import com.google.common.base.CharMatcher;

import io.dts.common.common.exception.DtsException;
import io.dts.parser.DtsObjectWapper;
import io.dts.parser.DtsSQLStatement;
import io.dts.parser.struct.DatabaseType;
import io.dts.parser.struct.TxcColumnMeta;
import io.dts.parser.struct.TxcTable;
import io.dts.parser.struct.TxcTableMeta;


public class DtsInsertVisitor extends AbstractDtsVisitor {


  public DtsInsertVisitor(DtsSQLStatement node, List<Object> parameterSet) {
    super(node, parameterSet);
  }

  @Override
  public boolean visit(final MySqlInsertStatement x) {
    final String tableName = getExactlyValue(x.getTableName().toString());
    setTableName(tableName);
    setTableNameAlias(x.getAlias());
    return super.visit(x);
  }

  @Override
  public TxcTable executeAndGetFrontImage(Statement st) throws SQLException {
    // insert前不需进行任何操作
    TxcTableMeta tableMeta = getTableMeta();
    TxcTable tableOriginalValue = getTableOriginalValue();
    tableOriginalValue.setTableMeta(tableMeta);
    tableOriginalValue.setTableName(tableMeta.getTableName());
    tableOriginalValue.setAlias(tableMeta.getAlias());
    tableOriginalValue.setSchemaName(tableMeta.getSchemaName());
    return tableOriginalValue;
  }

  @Override
  public TxcTable executeAndGetRearImage(Statement st) throws SQLException {
    String sql = getSelectSql() + getWhereCondition(st);

    TxcTable tablePresentValue = getTablePresentValue();
    TxcTableMeta tableMeta = getTableMeta();
    // SQL执行后查询DB行现值，用户脏读检查
    // 此时，还没有拿到数据的索引值，因此需要使用不带KEY的SQL去查询现值
    tablePresentValue.setTableMeta(tableMeta);
    tablePresentValue.setTableName(tableMeta.getTableName());
    tablePresentValue.setAlias(tableMeta.getAlias());
    tablePresentValue.setSchemaName(tableMeta.getSchemaName());
    tablePresentValue.setLines(addLines(sql));
    return tablePresentValue;
  }


  /**
   * 根据insert是否显式指定主键。<br>
   * 如果指定了主键 ： 以全部输入字段作为查询条件。<br>
   * 如果没有指定主键：去DB中获取last_insert_key，如果存在则使用自增主键为查询条件，否则以全部字段为查询条件<br>
   * 仅支持数字、字符串为查询条件，其他类型直接忽略掉，但是，如果其他类型被指定为主键，则抛异常。<br>
   */
  @SuppressWarnings("serial")
  @Override
  public String parseWhereCondition(Statement st) {

    SQLInsertStatement insertStatement = (SQLInsertStatement) getSQLStatement().getSQLStatement();
    List<SQLExpr> columns = insertStatement.getColumns();
    List<SQLExpr> sqlExprs = insertStatement.getValues().getValues();
    StringBuilder whereSqlAppender = new StringBuilder();

    if (getTableMeta().isContainsPriKey(new ArrayList<String>() {
      {
        for (SQLExpr column : columns) {
          add(((SQLIdentifierExpr) column).getName());
        }
      }
    })) {
      // 指定了主键
      selectByPK(whereSqlAppender, sqlExprs, columns);
    } else {
      // 没有指定主键
      selectByAutoIncreaseKey(whereSqlAppender, sqlExprs, columns, st);
    }

    return whereSqlAppender.toString();
  }


  private ValuePair evalExpression(final DatabaseType databaseType, final SQLObject sqlObject,
      final List<Object> parameters) {
    if (sqlObject instanceof SQLMethodInvokeExpr) {
      return null;
    }
    SQLEvalVisitor visitor;
    switch (databaseType.name().toLowerCase()) {
      case JdbcUtils.MYSQL:
      case JdbcUtils.H2:
        visitor = new MySQLEvalVisitor();
        break;
      default:
        visitor = SQLEvalVisitorUtils.createEvalVisitor(databaseType.name());
    }
    visitor.setParameters(parameters);
    sqlObject.accept(visitor);

    Object value = SQLEvalVisitorUtils.getValue(sqlObject);
    if (null == value) {
      // TODO 对于NULL目前解析为空字符串,此处待考虑解决方法
      return null;
    }

    Comparable<?> finalValue;
    if (value instanceof Comparable<?>) {
      finalValue = (Comparable<?>) value;
    } else {
      finalValue = "";
    }
    Integer index = (Integer) sqlObject.getAttribute(MySQLEvalVisitor.EVAL_VAR_INDEX);
    if (null == index) {
      index = -1;
    }
    return new ValuePair(finalValue, index);
  }


  private void selectByPK(StringBuilder appender, List<SQLExpr> itemsList, List<SQLExpr> cols) {
    for (int i = 0; i < itemsList.size(); i++) {
      String attrName = ((SQLIdentifierExpr) cols.get(i)).getName();
      TxcTableMeta tableMeta = getTableMeta();
      Map<String, TxcColumnMeta> index = tableMeta.getPrimaryKeyMap();

      TxcColumnMeta colMeta = tableMeta.getAllColumns().get(attrName.toUpperCase());
      if (colMeta == null) {
        colMeta = tableMeta.getAllColumns().get(attrName.toLowerCase());
      }

      if (colMeta.isCommonType() == false) {
        continue;
      }

      String tableName = colMeta.getTableName();
      String colName = colMeta.getColumnName().toUpperCase();
      if (index.get(colName) == null) {
        continue;
      }

      ValuePair valuePair =
          evalExpression(getSQLStatement().getDatabaseType(), itemsList.get(i), getParameters());

      attrName = String.format("%s.%s", tableName, attrName);
      DtsObjectWapper.appendParamMarkerObject(attrName, valuePair.value, appender);
      return;
    }
  }

  private void selectByAutoIncreaseKey(StringBuilder appender, List<SQLExpr> itemsList,
      List<SQLExpr> cols, Statement st) {
    TxcColumnMeta column = getTableMeta().getAutoIncreaseColumn();
    if (column == null) {
      throw new IllegalArgumentException("no auto increase column.");
    }

    try {
      ResultSet rs = null;
      try {
        rs = st.getGeneratedKeys();
      } catch (SQLException e) {
        // java.sql.SQLException: Generated keys not requested. You need
        // to
        // specify Statement.RETURN_GENERATED_KEYS to
        // Statement.executeUpdate() or Connection.prepareStatement().
        if (e.getSQLState().equalsIgnoreCase("S1009")) {
          rs = st.executeQuery("SELECT LAST_INSERT_ID()"); // 通过额外查询获取
        }
      }

      if (rs != null) {
        List<Object> parameterSet = getParameters();
        if (rs.next()) {
          if (parameterSet != null && parameterSet.size() > 0) {
            appender.append(column.getColumnName());
            appender.append("=");
            appender.append(rs.getObject(1));
          }
        } else {
          boolean bOrFlag = true;
          while (rs.next()) {
            if (bOrFlag) {
              bOrFlag = false;
            } else {
              appender.append(" OR ");
            }

            appender.append(column.getColumnName());
            appender.append("=");
            appender.append(rs.getObject(1));
          }
        }
        rs.close();
      }
      // 如果获取不到自增Key值，则退化成按所有字段进行搜索
      else {
        selectByAllFields(appender, itemsList, cols);
      }
    } catch (SQLException e) {
      throw new DtsException(e);
    }
  }

  private void selectByAllFields(StringBuilder appender, List<SQLExpr> itemsList,
      List<SQLExpr> cols) {
    setRow(cols, itemsList, 0, appender);
  }

  /**
   * 分解数据库属性名和值，拼装where语句后的判断条件
   *
   * @param cols 属性名列表
   * @param row 属性值组
   * @param loop 使用占位符赋值的属性处于List中的位置
   */
  private void setRow(List<SQLExpr> cols, List<SQLExpr> itemsList, int loop,
      StringBuilder appender) {

    for (int i = 0; i < itemsList.size(); i++) {
      String attrName = ((SQLIdentifierExpr) cols.get(i)).getName();
      TxcTableMeta tableMeta = getTableMeta();

      TxcColumnMeta colMeta = tableMeta.getAllColumns().get(attrName.toUpperCase());
      if (colMeta == null) {
        colMeta = tableMeta.getAllColumns().get(attrName.toLowerCase());
      }

      if (colMeta.isCommonType() == false) {
        continue;
      }

      String tableName = colMeta.getTableName();
      ValuePair valuePair =
          evalExpression(getSQLStatement().getDatabaseType(), itemsList.get(i), getParameters());
      attrName = String.format("%s.%s", tableName, attrName);
      DtsObjectWapper.appendParamMarkerObject(attrName, valuePair.getValue(), appender);
      return;
    }
  }

  public String getExactlyValue(final String value) {
    return null == value ? null : CharMatcher.anyOf("[]`'\"").removeFrom(value);
  }

  private static class ValuePair {

    private final Comparable<?> value;

    private final Integer paramIndex;


    public ValuePair(Comparable<?> value, Integer paramIndex) {
      super();
      this.value = value;
      this.paramIndex = paramIndex;
    }

    public Comparable<?> getValue() {
      return value;
    }

    @SuppressWarnings("unused")
    public Integer getParamIndex() {
      return paramIndex;
    }

  }

  private static class MySQLEvalVisitor extends MySqlEvalVisitorImpl {
    private static final String EVAL_VAR_INDEX = "EVAL_VAR_INDEX";

    @Override
    public boolean visit(final SQLVariantRefExpr x) {
      if (!"?".equals(x.getName())) {
        return false;
      }

      Map<String, Object> attributes = x.getAttributes();

      int varIndex = x.getIndex();

      if (varIndex == -1 || getParameters().size() <= varIndex) {
        return false;
      }
      if (attributes.containsKey(EVAL_VALUE)) {
        return false;
      }
      Object value = getParameters().get(varIndex);
      if (value == null) {
        value = EVAL_VALUE_NULL;
      }
      attributes.put(EVAL_VALUE, value);
      attributes.put(EVAL_VAR_INDEX, varIndex);
      return false;
    }
  }



}
