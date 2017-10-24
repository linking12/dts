/*
 * Copyright 1999-2015 dangdang.com. <p> Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License. </p>
 */

package io.dts.datasource.executor.prepared;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import io.dts.datasource.executor.BaseStatementUnit;
import io.dts.datasource.executor.ExecuteCallback;
import io.dts.datasource.executor.ExecutorEngine;


public final class PreparedStatementExecutor {


  private final ExecutorEngine executorEngine;

  private final PreparedStatementUnit preparedStatementUnit;

  private final List<Object> parameters;

  public PreparedStatementExecutor(ExecutorEngine executorEngine,
      PreparedStatementUnit preparedStatementUnit, List<Object> parameters) {
    super();
    this.executorEngine = executorEngine;
    this.preparedStatementUnit = preparedStatementUnit;
    this.parameters = parameters;
  }



  /**
   * Execute query.
   * 
   * @return result set list
   */
  public ResultSet executeQuery() throws Exception {
    return executorEngine.executePreparedStatement(preparedStatementUnit, parameters,
        new ExecuteCallback<ResultSet>() {

          @Override
          public ResultSet execute(final BaseStatementUnit baseStatementUnit) throws Exception {
            return ((PreparedStatement) baseStatementUnit.getStatement().getRawStatement())
                .executeQuery();
          }
        });
  }

  /**
   * Execute update.
   * 
   * @return effected records count
   */
  public int executeUpdate() throws Exception {
    Integer results = executorEngine.executePreparedStatement(preparedStatementUnit, parameters,
        new ExecuteCallback<Integer>() {

          @Override
          public Integer execute(final BaseStatementUnit baseStatementUnit) throws Exception {
            return ((PreparedStatement) baseStatementUnit.getStatement().getRawStatement())
                .executeUpdate();
          }
        });
    return results;
  }

  /**
   * Execute SQL.
   *
   * @return return true if is DQL, false if is DML
   */
  public boolean execute() throws Exception {
    Boolean result = executorEngine.executePreparedStatement(preparedStatementUnit, parameters,
        new ExecuteCallback<Boolean>() {

          @Override
          public Boolean execute(final BaseStatementUnit baseStatementUnit) throws Exception {
            return ((PreparedStatement) baseStatementUnit.getStatement().getRawStatement())
                .execute();
          }
        });
    if (null == result) {
      return false;
    }
    return result;
  }
}
