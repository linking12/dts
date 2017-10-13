package io.dts.server.handler;

/**
 * @author jiangyu.jy
 * 
 *         事务分支的回滚状态
 */
public enum RollbackingResultCode {
  /**
   * 分支回滚已发出，还没有响应
   */
  BEGIN(1),

  /**
   * 分支回滚失败
   */
  FAILED(2),

  /**
   * 分支回滚超时
   */
  TIMEOUT(3),

  /**
   * 分支回滚已经成功，但是从本地数据库删除分支记录失败
   */
  UPDATELOCALFAILED(4);

  /**
   * @param value
   */
  private RollbackingResultCode(int value) {
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
