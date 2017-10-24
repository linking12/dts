
package io.dts.datasource.executor.statement;



import io.dts.datasource.executor.BaseStatementUnit;
import io.dts.datasource.executor.SQLExecutionUnit;
import io.dts.resourcemanager.api.IDtsStatement;



public final class StatementUnit implements BaseStatementUnit {

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



}
