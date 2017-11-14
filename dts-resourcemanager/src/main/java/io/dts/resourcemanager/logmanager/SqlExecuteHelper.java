/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.dts.resourcemanager.logmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;

import io.dts.resourcemanager.DataSourceHolder;

/**
 * @author liushiming
 * @version SqlExecuteHelper.java, v 0.0.1 2017年11月8日 下午3:22:27 liushiming
 */
public class SqlExecuteHelper {

  private static final Logger logger = LoggerFactory.getLogger(SqlExecuteHelper.class);

  public static void executeSql(String dbName, String sql) throws SQLException {
    if (sql == null) {
      return;
    }
    try {
      DataSource db = DataSourceHolder.getDataSource(dbName);
      JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
      jdbcTemplate.execute(sql);
    } catch (DataAccessException e) {
      SQLException sqle = (SQLException) e.getCause();
      if (sqle.getErrorCode() == 1062) {
        logger.info("RtExecutor retry sql:" + e.getMessage() + ":" + sql);
      } else {
        throw sqle;
      }
    }
  }

  public static <T> List<T> querySql(final JdbcTemplate template, final RowMapper<T> rowMapper,
      final String sql) {
    List<T> contents;
    long start = 0;
    if (logger.isDebugEnabled())
      start = System.currentTimeMillis();
    try {
      contents = template.query(sql, rowMapper);
    } finally {
      if (logger.isDebugEnabled()) {
        long end = System.currentTimeMillis();
        logger.info(String.format("query:[%s] cost %d ms.", sql, (end - start)));
      }
    }

    return contents;
  }


  public static <T> T executeSql(final Connection connection, String sql,
      PreparedStatementCallback<T> callback) throws SQLException {
    PreparedStatement pst = null;
    try {
      pst = connection.prepareStatement(sql);
      return callback.doInPreparedStatement(pst);
    } finally {
      if (pst != null) {
        pst.close();
      }
    }
  }
}
