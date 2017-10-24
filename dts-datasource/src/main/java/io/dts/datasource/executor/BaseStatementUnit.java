
package io.dts.datasource.executor;

import io.dts.resourcemanager.api.IDtsStatement;


public interface BaseStatementUnit {

  /**
   * Get SQL execute unit.
   * 
   * @return SQL execute unit
   */
  SQLExecutionUnit getSqlExecutionUnit();

  /**
   * Get statement.
   * 
   * @return statement
   */
  IDtsStatement getStatement();
}
