package io.dts.client.template;


/**
 * @author qinan.qn@taobao.com 2014年10月30日
 */
public interface TxcCallback {
  /**
   * 事务模板回调函数，用以模板开启事务后，调用此方法执行业务逻辑
   * 
   * @return
   * @throws Throwable 用户抛出的异常
   * @since 1.1.0
   */
  public Object callback() throws Throwable;
}
