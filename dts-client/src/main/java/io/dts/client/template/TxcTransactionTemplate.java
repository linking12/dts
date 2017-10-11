package io.dts.client.template;



import io.dts.client.api.DtsTransactionManager;
import io.dts.client.api.impl.DefaultDtsTransactionManager;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;


public class TxcTransactionTemplate {
  private DtsTransactionManager tm = DefaultDtsTransactionManager.getInstance();

  /**
   * 
   * @param callback 回调函数，在事务开启后，将回调此方法
   * @param timeout 事务超时时间，单位ms
   * @return
   * @throws TxcException 当sql语句执行异常或TXC client无法连接到TXC server时，抛出此异常
   * @since 1.1.0
   */
  public Object runATMT(TxcCallback callback, long timeout) throws DtsException {
    try {
      tm.begin(timeout);
      Object obj = callback.callback();
      tm.commit();
      return obj;
    } catch (Throwable e) {
      try {
        tm.rollback();
      } catch (Throwable ee) {
      }
      throw new DtsException(e);
    }
  }

  /**
   * 
   * @param callback 回调函数，在事务开启后，将回调此方法
   * @param effectiveTime RT模式下，TXC将在此时间内，保证重试callback中失败的sql语句
   * @return
   * @throws TxcException 当sql语句执行异常或TXC client无法连接到TXC server时，抛出此异常
   * @since 1.1.0
   */
  public Object runRT(TxcCallback callback, long effectiveTime) throws DtsException {
    try {
      DtsContext.startRetryBranch(effectiveTime);
      Object obj = callback.callback();
      return obj;
    } catch (Throwable e) {
      throw new DtsException(e);
    } finally {
      DtsContext.endRetryBranch();
    }
  }
}
