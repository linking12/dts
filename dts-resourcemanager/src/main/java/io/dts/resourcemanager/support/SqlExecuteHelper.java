package io.dts.resourcemanager.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by guoyubo on 2017/9/27.
 */
public class SqlExecuteHelper {

  private static final Logger logger = LoggerFactory.getLogger(SqlExecuteHelper.class);

  private static String txcLogTableName = "txc_undo_log";


  public static void executeSql(String dbName, String retrySql) throws SQLException {
    if (retrySql == null) {
      return;
    }
    try {
      DataSource db = DataSourceHolder.getDataSource(dbName);
      JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
      jdbcTemplate.execute(retrySql);
    } catch (DataAccessException e) {
      // SQLState:23000
      // VendorCode:1062
      // Duplicate entry key 'PRIMARY'
      SQLException sqle = (SQLException) e.getCause();
      if (sqle.getErrorCode() == 1062) {
        logger.info("RtExecutor retry sql:" + e.getMessage() + ":" + retrySql);
      } else {
        throw sqle;
      }
    }
  }

  public static <T> List<T> querySql(final JdbcTemplate template,
      final RowMapper rowMapper, final String sql) {
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


  public static <T> T executeSql(final Connection connection, String sql, PreparedStatementCallback<T> callback)
      throws SQLException {
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
