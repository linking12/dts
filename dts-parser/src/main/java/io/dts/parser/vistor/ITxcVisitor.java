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

package io.dts.parser.vistor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import io.dts.parser.DtsSQLStatement;
import io.dts.parser.struct.SqlType;
import io.dts.parser.struct.TxcTable;
import io.dts.parser.struct.TxcTableMeta;

/**
 * SQL解析基础访问器接口.
 * 
 */
public interface ITxcVisitor {

  void setConnection(Connection connection);

  TxcTableMeta buildTableMeta() throws SQLException;

  /**
   * 获取前置镜像
   *
   * @throws SQLException
   */
  TxcTable executeAndGetFrontImage(Statement st) throws SQLException;


  TxcTable getTableOriginalValue() throws SQLException;

  /**
   * 获取后置镜像
   *
   * @throws SQLException
   */
  TxcTable executeAndGetRearImage(Statement st) throws SQLException;

  TxcTable getTablePresentValue() throws SQLException;

  /**
   * 获取原始数据的SQL
   *
   * @return
   */
  String getInputSql();

  /**
   * 获取原始数据的SQL
   *
   * @return
   */
  String getFullSql();

  /**
   * 查询SQL，用于取得DB行变更前后镜像
   *
   * @return
   */
  String getSelectSql() throws SQLException;


  String getWhereCondition(TxcTable table);


  String getTableName() throws SQLException;

  SqlType getSqlType();



  TxcTableMeta getTableMeta();

  DtsSQLStatement getSQLStatement();


}
