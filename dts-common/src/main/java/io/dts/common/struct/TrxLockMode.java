package io.dts.common.struct;


/**
 * @TODO 增加事务隔离级别是否需要？
 * 
 * 
 * @author liushiming
 * @version TrxLockMode.java, v 0.0.1 2017年10月27日 下午5:52:32 liushiming
 */
public enum TrxLockMode {
  DELETE_TRX_LOCK(1), NOT_DELETE_TRX_LOCK(0);

  private TrxLockMode(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  private int value;
}
