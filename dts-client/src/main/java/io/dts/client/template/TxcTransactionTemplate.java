package io.dts.client.template;



import io.dts.client.api.DtsTransactionManager;
import io.dts.client.api.impl.DefaultDtsTransactionManager;
import io.dts.common.common.context.DtsContext;
import io.dts.common.common.exception.DtsException;


public class TxcTransactionTemplate {
  private DtsTransactionManager tm = DefaultDtsTransactionManager.getInstance();


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
