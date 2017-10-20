package io.dts.datasource.executor;

import java.sql.SQLException;

import io.dts.parser.constant.SqlType;
import io.dts.parser.util.SqlTypeParser;
import io.dts.resourcemanager.api.IDtsDataSource;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Created by guoyubo on 2017/9/21.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class SQLExecutionUnit {

  private final IDtsDataSource dataSource;

  private final String sql;


  public SqlType getSqlType() throws SQLException {
    return SqlTypeParser.getSqlType(sql);
  }

}

