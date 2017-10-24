
package io.dts.parser.vistor;


import java.util.HashMap;
import java.util.Map;

import io.dts.parser.constant.DatabaseType;
import io.dts.parser.vistor.base.TxcDeleteVisitor;
import io.dts.parser.vistor.base.TxcInsertVisitor;
import io.dts.parser.vistor.base.TxcSelectVisitor;
import io.dts.parser.vistor.base.TxcUpdateVisitor;


public final class SQLVisitorRegistry {

  private static final Map<DatabaseType, Class<? extends ITxcVisitor>> SELECT_REGISTRY =
      new HashMap<>(DatabaseType.values().length);

  private static final Map<DatabaseType, Class<? extends ITxcVisitor>> INSERT_REGISTRY =
      new HashMap<>(DatabaseType.values().length);

  private static final Map<DatabaseType, Class<? extends ITxcVisitor>> UPDATE_REGISTRY =
      new HashMap<>(DatabaseType.values().length);

  private static final Map<DatabaseType, Class<? extends ITxcVisitor>> DELETE_REGISTRY =
      new HashMap<>(DatabaseType.values().length);

  static {
    registerSelectVistor();
    registerInsertVistor();
    registerUpdateVistor();
    registerDeleteVistor();
  }

  private static void registerSelectVistor() {
    SELECT_REGISTRY.put(DatabaseType.H2, TxcSelectVisitor.class);
    SELECT_REGISTRY.put(DatabaseType.MySQL, TxcSelectVisitor.class);
    // TODO 其他数据库先使用MySQL, 只能使用标准SQL
    SELECT_REGISTRY.put(DatabaseType.Oracle, TxcSelectVisitor.class);
    SELECT_REGISTRY.put(DatabaseType.SQLServer, TxcSelectVisitor.class);
    SELECT_REGISTRY.put(DatabaseType.DB2, TxcSelectVisitor.class);
    SELECT_REGISTRY.put(DatabaseType.PostgreSQL, TxcSelectVisitor.class);
  }

  private static void registerInsertVistor() {
    INSERT_REGISTRY.put(DatabaseType.H2, TxcInsertVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.MySQL, TxcInsertVisitor.class);
    // TODO 其他数据库先使用MySQL, 只能使用标准SQL
    INSERT_REGISTRY.put(DatabaseType.Oracle, TxcInsertVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.SQLServer, TxcInsertVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.DB2, TxcInsertVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.PostgreSQL, TxcInsertVisitor.class);
  }

  private static void registerUpdateVistor() {
    UPDATE_REGISTRY.put(DatabaseType.H2, TxcUpdateVisitor.class);
    UPDATE_REGISTRY.put(DatabaseType.MySQL, TxcUpdateVisitor.class);
    // TODO 其他数据库先使用MySQL, 只能使用标准SQL
    INSERT_REGISTRY.put(DatabaseType.Oracle, TxcUpdateVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.SQLServer, TxcUpdateVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.DB2, TxcUpdateVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.PostgreSQL, TxcUpdateVisitor.class);
  }

  private static void registerDeleteVistor() {
    DELETE_REGISTRY.put(DatabaseType.H2, TxcDeleteVisitor.class);
    DELETE_REGISTRY.put(DatabaseType.MySQL, TxcDeleteVisitor.class);
    // TODO 其他数据库先使用MySQL, 只能使用标准SQL
    INSERT_REGISTRY.put(DatabaseType.Oracle, TxcDeleteVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.SQLServer, TxcDeleteVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.DB2, TxcDeleteVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.PostgreSQL, TxcDeleteVisitor.class);
  }

  /**
   * 获取SELECT访问器.
   * 
   * @param databaseType 数据库类型
   * @return SELECT访问器
   */
  public static Class<? extends ITxcVisitor> getSelectVistor(final DatabaseType databaseType) {
    return getVistor(databaseType, SELECT_REGISTRY);
  }

  /**
   * 获取INSERT访问器.
   * 
   * @param databaseType 数据库类型
   * @return INSERT访问器
   */
  public static Class<? extends ITxcVisitor> getInsertVistor(final DatabaseType databaseType) {
    return getVistor(databaseType, INSERT_REGISTRY);
  }

  /**
   * 获取UPDATE访问器.
   * 
   * @param databaseType 数据库类型
   * @return UPDATE访问器
   */
  public static Class<? extends ITxcVisitor> getUpdateVistor(final DatabaseType databaseType) {
    return getVistor(databaseType, UPDATE_REGISTRY);
  }

  /**
   * 获取DELETE访问器.
   * 
   * @param databaseType 数据库类型
   * @return DELETE访问器
   */
  public static Class<? extends ITxcVisitor> getDeleteVistor(final DatabaseType databaseType) {
    return getVistor(databaseType, DELETE_REGISTRY);
  }

  private static Class<? extends ITxcVisitor> getVistor(final DatabaseType databaseType,
      final Map<DatabaseType, Class<? extends ITxcVisitor>> registry) {
    if (!registry.containsKey(databaseType)) {
      throw new UnsupportedOperationException(databaseType.name());
    }
    return registry.get(databaseType);
  }
}
