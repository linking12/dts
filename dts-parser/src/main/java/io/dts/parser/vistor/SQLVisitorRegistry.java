
package io.dts.parser.vistor;


import java.util.HashMap;
import java.util.Map;

import io.dts.parser.struct.DatabaseType;
import io.dts.parser.vistor.mysql.DtsDeleteVisitor;
import io.dts.parser.vistor.mysql.DtsInsertVisitor;
import io.dts.parser.vistor.mysql.DtsSelectVisitor;
import io.dts.parser.vistor.mysql.DtsUpdateVisitor;


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
    SELECT_REGISTRY.put(DatabaseType.H2, DtsSelectVisitor.class);
    SELECT_REGISTRY.put(DatabaseType.MySQL, DtsSelectVisitor.class);
    // TODO 其他数据库先使用MySQL, 只能使用标准SQL
    SELECT_REGISTRY.put(DatabaseType.Oracle, DtsSelectVisitor.class);
    SELECT_REGISTRY.put(DatabaseType.SQLServer, DtsSelectVisitor.class);
    SELECT_REGISTRY.put(DatabaseType.DB2, DtsSelectVisitor.class);
    SELECT_REGISTRY.put(DatabaseType.PostgreSQL, DtsSelectVisitor.class);
  }

  private static void registerInsertVistor() {
    INSERT_REGISTRY.put(DatabaseType.H2, DtsInsertVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.MySQL, DtsInsertVisitor.class);
    // TODO 其他数据库先使用MySQL, 只能使用标准SQL
    INSERT_REGISTRY.put(DatabaseType.Oracle, DtsInsertVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.SQLServer, DtsInsertVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.DB2, DtsInsertVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.PostgreSQL, DtsInsertVisitor.class);
  }

  private static void registerUpdateVistor() {
    UPDATE_REGISTRY.put(DatabaseType.H2, DtsUpdateVisitor.class);
    UPDATE_REGISTRY.put(DatabaseType.MySQL, DtsUpdateVisitor.class);
    // TODO 其他数据库先使用MySQL, 只能使用标准SQL
    INSERT_REGISTRY.put(DatabaseType.Oracle, DtsUpdateVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.SQLServer, DtsUpdateVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.DB2, DtsUpdateVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.PostgreSQL, DtsUpdateVisitor.class);
  }

  private static void registerDeleteVistor() {
    DELETE_REGISTRY.put(DatabaseType.H2, DtsDeleteVisitor.class);
    DELETE_REGISTRY.put(DatabaseType.MySQL, DtsDeleteVisitor.class);
    // TODO 其他数据库先使用MySQL, 只能使用标准SQL
    INSERT_REGISTRY.put(DatabaseType.Oracle, DtsDeleteVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.SQLServer, DtsDeleteVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.DB2, DtsDeleteVisitor.class);
    INSERT_REGISTRY.put(DatabaseType.PostgreSQL, DtsDeleteVisitor.class);
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
