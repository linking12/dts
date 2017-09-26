package io.dts.parser.constant;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import java.util.Arrays;

/**
 * Created by guoyubo on 2017/9/20.
 */
public enum DatabaseType {
  H2("H2"), MySQL("MySQL"), Oracle("Oracle"), SQLServer("Microsoft SQL Server"), PostgreSQL("PostgreSQL");

  private final String productName;

  DatabaseType(final String productName) {
    this.productName = productName;
  }

  /**
   * Get database type enum via database name string.
   *
   * @param databaseProductName database name string
   * @return database enum
   */
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
