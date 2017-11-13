package io.dts.client.template;



import io.dts.client.api.DtsTransactionManager;
import io.dts.client.api.impl.DefaultDtsTransactionManager;


public class TxcTransactionTemplate {
  private DtsTransactionManager tm = DefaultDtsTransactionManager.getInstance();


  public Object run(TxcCallback callback, long timeout) throws Throwable {
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
      throw e;
    }
  }

}
