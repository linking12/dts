package io.dts.server.struct;

/**
 * 分支状态
 */
public enum BranchLogState {

  /**
   * 分支开始
   */
  Begin(1),

  /**
   * 分支成功
   */
  Success(2),

  /**
   * 分支失败
   */
  Failed(3);


  /**
   * @param value
   */
  private BranchLogState(int value) {
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
}
