package io.dts.parser.constant;

import com.alibaba.druid.util.JdbcUtils;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import java.util.Arrays;

/**
 * Created by guoyubo on 2017/9/20.
 */
public enum DatabaseType {
  
  H2("H2", JdbcUtils.H2),
  
  MySQL("MySQL", JdbcUtils.MYSQL), 
  
  Oracle("Oracle", JdbcUtils.ORACLE),
  
  SQLServer("Microsoft SQL Server", JdbcUtils.SQL_SERVER),
  
  PostgreSQL("PostgreSQL", JdbcUtils.POSTGRESQL),
  
  DB2("DB2", JdbcUtils.DB2);

  private final String productName;
  private final String druidSqlType;

  DatabaseType(final String productName, final String druidSqlType) {
    this.productName = productName;
    this.druidSqlType = druidSqlType;
  }

  public String getDruidSqlType() {
    return druidSqlType;
  }

  
  public static DatabaseType valueFrom(final String databaseProductName) {
    Optional<DatabaseType> databaseTypeOptional = Iterators.tryFind(Arrays.asList(DatabaseType.values()).iterator(), new Predicate<DatabaseType>() {

      @Override
      public boolean apply(final DatabaseType input) {
        return input.productName.equals(databaseProductName);
      }
    });
    if (databaseTypeOptional.isPresent()) {
      return databaseTypeOptional.get();
    }
    throw new UnsupportedOperationException(String.format("Can not support database type [%s].", databaseProductName));
  }
}
