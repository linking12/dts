package io.dts.parser.vistor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.dts.common.common.exception.DtsException;
import io.dts.parser.struct.IndexType;
import io.dts.parser.struct.TxcColumnMeta;
import io.dts.parser.struct.TxcIndex;
import io.dts.parser.struct.TxcTableMeta;

/**
 * Created by guoyubo on 2017/9/25.
 */
public class DtsTableMetaTools {

  private static long cacheSize = 1000;
  private static long expireTime = 300 * 1000;
  private final static Cache<String, TxcTableMeta> tableMetaCache =
      CacheBuilder.newBuilder().maximumSize(cacheSize)
          .expireAfterWrite(expireTime, TimeUnit.MILLISECONDS).softValues().build();


  private static Logger logger = LoggerFactory.getLogger(DtsTableMetaTools.class);

  public static TxcTableMeta getTableMeta(final String schemaName, final String tableName) {
    if (tableName == null || tableName.isEmpty()) {
      throw new DtsException("table " + tableName + " cannot fetched without tableName");
    }

    TxcTableMeta tmeta = null;
    try {
      tmeta = tableMetaCache.get(tableName, new Callable<TxcTableMeta>() {
        @Override
        public TxcTableMeta call() throws Exception {
          return null;
        }
      });
    } catch (ExecutionException e) {
      logger.error("1111", "tableMeta cache error", e.getMessage());
    }

    return tmeta;
  }

  public static TxcTableMeta getTableMeta(final Connection conn, final String tableName) {
    if (tableName == null || tableName.isEmpty()) {
      throw new DtsException("table " + tableName + " cannot fetched without tableName");
    }

    TxcTableMeta tmeta = null;
    try {
      tmeta = tableMetaCache.get(tableName, new Callable<TxcTableMeta>() {
        @Override
        public TxcTableMeta call() throws Exception {
          return getTableMeta0(conn, tableName);
        }
      });
    } catch (ExecutionException e) {
      logger.error("1111", "tableMeta cache error", e.getMessage());
    }

    if (tmeta == null) {
      tmeta = getTableMeta0(conn, tableName);
    }

    return tmeta;
  }

  private static TxcTableMeta getTableMeta0(Connection conn, String tableName) {
    TxcTableMeta tmeta = null;
    try {
      tmeta = fetchSchema(conn, tableName);
    } catch (SQLException e) {
      throw new DtsException(e, "getTableMeta error");
      // 此处异常处理，不要轻易调整，因为redo commit过程，会截取当前txc异常，用以判断是否
      // sql异常，以决定是否终止commit过程。
    }
    return tmeta;
  }

  private static TxcTableMeta fetchSchema(Connection conn, String tableName) throws SQLException {
    return fetchSchema0(conn, tableName);
  }

  @SuppressWarnings("resource")
  private static TxcTableMeta fetchSchema0(Connection conn, String tableName) throws SQLException {
    java.sql.Statement stmt = null;
    java.sql.ResultSet rs = null;
    try {
      stmt = conn.createStatement();
      rs = stmt.executeQuery("select * from " + tableName + " limit 1");
      java.sql.ResultSetMetaData rsmd = rs.getMetaData();
      java.sql.DatabaseMetaData dbmd = conn.getMetaData();

      return resultSetMetaToSchema(rsmd, dbmd);
    } catch (Exception e) {
      if (e instanceof SQLException) {
        if ("42000".equals(((SQLException) e).getSQLState())) {
          try {
            rs = stmt.executeQuery("select * from " + tableName + " where rownum <= 2");
            java.sql.ResultSetMetaData rsmd = rs.getMetaData();
            java.sql.DatabaseMetaData dbmd = conn.getMetaData();

            return resultSetMetaToSchema(rsmd, dbmd);
          } catch (SQLException e1) {
            logger.warn("{}", e);
          }
        }
        throw (SQLException) e; // 千万不要吞异常，否则后面没法处理。。。
      }
      logger.error("1111", "schema of " + tableName + " cannot be fetched", e);
      return null;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  // txc_client-null-student-roll_number-4-INT-10-0-10-0--null-0-00-1-NO-YES-
  // txc_client-null-student-name-12-VARCHAR-20-0-10-1--null-0-020-2-YES-NO-
  // txc_client-null-student-class-12-VARCHAR-5-0-10-1--null-0-05-3-YES-NO-
  // txc_client-null-student-section-12-VARCHAR-5-0-10-1--null-0-05-4-YES-NO-
  // txc_client-null-student-gmt_create-93-TIMESTAMP-19-0-10-0--0000-00-00
  // 00:00:00-0-00-5-NO-NO-
  // txc_client-null-student-id-4-INT-10-0-10-0--null-0-00-6-NO-NO-
  // txc_client-null-student-gmt_modified-93-TIMESTAMP-19-0-10-0--0000-00-00
  // 00:00:00-0-00-7-NO-NO-
  // false--bbb-3-1-roll_number-A-0
  // false--bbb-3-2-id-A-0
  // false--PRIMARY-3-1-id-A-0
  // true--aaa-3-1-roll_number-A-0
  // true--aaa-3-2-name-A-0
  private static TxcTableMeta resultSetMetaToSchema(ResultSetMetaData rsmd, DatabaseMetaData dbmd)
      throws SQLException {
    String tableName = rsmd.getTableName(1);
    String schemaName = rsmd.getSchemaName(1);
    String catalogName = rsmd.getCatalogName(1);

    TxcTableMeta tm = new TxcTableMeta();
    tm.setTableName(tableName);
    if (schemaName == null || schemaName.isEmpty()) {
      tm.setSchemaName(catalogName);
    } else {
      tm.setSchemaName(schemaName);
    }

    java.sql.ResultSet rs1 = dbmd.getColumns(catalogName, schemaName, tableName, "%");
    while (rs1.next()) {
      TxcColumnMeta col = new TxcColumnMeta();
      col.setTableCat(rs1.getString("TABLE_CAT"));
      col.setTableSchemaName(rs1.getString("TABLE_SCHEM"));
      col.setTableName(rs1.getString("TABLE_NAME"));
      col.setColumnName(rs1.getString("COLUMN_NAME").toUpperCase());
      col.setDataType(rs1.getInt("DATA_TYPE"));
      col.setDataTypeName(rs1.getString("TYPE_NAME"));
      col.setColumnSize(rs1.getInt("COLUMN_SIZE"));
      col.setDecimalDigits(rs1.getInt("DECIMAL_DIGITS"));
      col.setNumPrecRadix(rs1.getInt("NUM_PREC_RADIX"));
      col.setNullAble(rs1.getInt("NULLABLE"));
      col.setRemarks(rs1.getString("REMARKS"));
      col.setColumnDef(rs1.getString("COLUMN_DEF"));
      col.setSqlDataType(rs1.getInt("SQL_DATA_TYPE"));
      col.setSqlDatetimeSub(rs1.getInt("SQL_DATETIME_SUB"));
      col.setCharOctetLength(rs1.getInt("CHAR_OCTET_LENGTH"));
      col.setOrdinalPosition(rs1.getInt("ORDINAL_POSITION"));
      col.setIsNullAble(rs1.getString("IS_NULLABLE"));
      col.setIsAutoincrement(rs1.getString("IS_AUTOINCREMENT"));

      tm.getAllColumns().put(col.getColumnName(), col);
    }

    java.sql.ResultSet rs2 = dbmd.getIndexInfo(catalogName, schemaName, tableName, false, true);
    while (rs2.next()) {
      String indexName = rs2.getString("INDEX_NAME");
      String colName = rs2.getString("COLUMN_NAME").toUpperCase();
      TxcColumnMeta col = tm.getAllColumns().get(colName);

      if (tm.getAllIndexes().containsKey(indexName)) {
        TxcIndex index = tm.getAllIndexes().get(indexName);
        index.getValues().add(col);
      } else {
        TxcIndex index = new TxcIndex();
        index.setIndexName(indexName);
        index.setNonUnique(rs2.getBoolean("NON_UNIQUE"));
        index.setIndexQualifier(rs2.getString("INDEX_QUALIFIER"));
        index.setIndexName(rs2.getString("INDEX_NAME"));
        index.setType(rs2.getShort("TYPE"));
        index.setOrdinalPosition(rs2.getShort("ORDINAL_POSITION"));
        index.setAscOrDesc(rs2.getString("ASC_OR_DESC"));
        index.setCardinality(rs2.getInt("CARDINALITY"));
        index.getValues().add(col);
        if ("PRIMARY".equalsIgnoreCase(indexName)) {
          index.setIndextype(IndexType.PRIMARY);
        } else if (index.isNonUnique() == false) {
          index.setIndextype(IndexType.Unique);
        } else {
          index.setIndextype(IndexType.Normal);
        }
        tm.getAllIndexes().put(indexName, index);
      }
    }

    return tm;
  }

}
