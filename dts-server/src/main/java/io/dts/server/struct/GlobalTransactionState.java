package io.dts.server.struct;

/**
 * 
 * 全局事务状态
 */
public enum GlobalTransactionState {

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
   * 未知状态，已发送请求，但是未返回成功结果，同步未返回成功结果
   */
  InDoubt(4);

  /**
   * @param value
   */
  private GlobalTransactionState(int value) {
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

  public static GlobalTransactionState parse(int value) {
    for (GlobalTransactionState state : GlobalTransactionState.values()) {
      if (state.getValue() == value) {
        return state;
      }
    }
    return null;
  }
}
