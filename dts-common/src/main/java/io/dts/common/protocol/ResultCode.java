package io.dts.common.protocol;


public enum ResultCode {
  /**
   * 成功
   */
  OK(1),

  /**
   * 失败，可恢复错误，需要重试
   */
  ERROR(0);


  private ResultCode(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  private int value;
}
