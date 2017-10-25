package io.dts.datasource;

/**
 * Created by guoyubo on 2017/9/20.
 */
public enum UndoLogMode {
  /**
   * 正常日志
   */
  COMMON_LOG(0),

  /**
   * 错误日志
   */
  ERROR_LOG(1),

  /**
   * 已删除日志
   */
  DELETED_LOG(2),

  /**
   * rt journel for record servers that has been connected by rt-rm
   */
  RT_JOURNEL(3),

  /**
   * 数据库表Meta
   */
  TABLE_MATA(9001);

  private UndoLogMode(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  private int value;
}
