package io.dts.parser.util;

import java.sql.SQLException;
import java.util.regex.Pattern;

import io.dts.parser.constant.SqlType;

/**
 * Created by guoyubo on 2017/9/23.
 */
public class SqlTypeParser {

  /**
   * 用于判断是否是一个select ... for update的sql
   */
  private static final Pattern SELECT_FOR_UPDATE_PATTERN =
      Pattern.compile("^select\\s+.*\\s+for\\s+update.*$", Pattern.CASE_INSENSITIVE);

  /**
   * 获得SQL语句种类
   */
  public static SqlType getSqlType(String sql) throws SQLException {
    SqlType sqlType = null;
    String noCommentsSql = sql;
    if (sql.contains("/*")) {
      noCommentsSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true).trim();
    }
    if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "select")) {
      if (noCommentsSql.toLowerCase().contains(" for ")
          && SELECT_FOR_UPDATE_PATTERN.matcher(noCommentsSql).matches()) {
        throw new SQLException(
            "only select, insert, update, delete,replace,truncate,create,drop,load,merge sql is supported");
      } else {
        sqlType = SqlType.SELECT;
      }
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "insert")) {
      sqlType = SqlType.INSERT;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "update")) {
      sqlType = SqlType.UPDATE;
    } else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "delete")) {
      sqlType = SqlType.DELETE;
    } else {
      throw new SQLException(
          "only select, insert, update, delete,replace,truncate,create,drop,load,merge sql is supported");
    }
    return sqlType;
  }


}
