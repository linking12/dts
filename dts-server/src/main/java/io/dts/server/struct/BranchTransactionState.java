package io.dts.server.struct;

/**
 * 事务分支的提交状态
 */
public enum BranchTransactionState {

  /**
   * 分支提交已发出，还没有响应
   */
  BEGIN(1),

  /**
   * 分支提交失败
   */
  FAILED(2),

  /**
   * 分支提交超时
   */
  TIMEOUT(3);

  /**
   * @param value
   */
  private BranchTransactionState(int value) {
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
