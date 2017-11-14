package io.dts.server.struct;

/**
 * 
 * 全局事务状态
 */
public enum GlobalLogState {

  /**
   * 开始
   */
  Begin(1),

  /**
   * 已提交
   */
  Committed(2),

  /**
   * 已回滚
   */
  Rollbacked(3),

  /**
   * 提交失败
   */
  CmmittedFailed(4),

  /**
   * 回滚失败
   */
  RollbackFailed(5);
  /**
   * @param value
   */
  private GlobalLogState(int value) {
    this.value = value;
  }

  /**
   * @return
   */
  public int getValue() {
    return value;
  }

  /**
   * 状态值
   */
  private int value;

  public static GlobalLogState parse(int value) {
    for (GlobalLogState state : GlobalLogState.values()) {
      if (state.getValue() == value) {
        return state;
      }
    }
    return null;
  }
}
