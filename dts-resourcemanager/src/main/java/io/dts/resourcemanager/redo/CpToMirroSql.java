package io.dts.resourcemanager.redo;

/**
 * @author qinan.qn@taobao.com 2015年3月27日
 */
public class CpToMirroSql {
  private String sql;
  private String originTableName;

  /**
   * @param sql
   * @param originTableName
   * @param param
   */
  public CpToMirroSql(String sql, String originTableName) {
    super();
    this.sql = sql;
    this.originTableName = originTableName;
  }

  public String getSql() {
    return sql;
  }

  public String getOriginTableName() {
    return originTableName;
  }
}
