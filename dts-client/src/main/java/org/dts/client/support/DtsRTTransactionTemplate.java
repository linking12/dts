package org.dts.client.support;

import org.dts.client.DtsTransactionManager;
import org.dts.client.exception.DtsTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.common.DtsContext;
import com.quancheng.dts.exception.DtsException;

/**
 * Created by guoyubo on 2017/8/28.
 */
public class DtsRTTransactionTemplate implements DtsTransactionOperations {

  private static final Logger logger = LoggerFactory.getLogger(DtsRTTransactionTemplate.class);

  private DtsTransactionManager transactionManager;

  public DtsRTTransactionTemplate(final DtsTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  /**
   *
   * @param callback
   *            回调函数，在事务开启后，将回调此方法
   * @param effectiveTime
   *            RT模式下，Dts将在此时间内，保证重试callback中失败的sql语句
   * @return
   * @throws DtsException
   *             当sql语句执行异常或TXC client无法连接到Dts server时，抛出此异常
   * @since 1.1.0
   */
  @Override
  public <T> T execute(DtsTransactionCallback<T> callback, long effectiveTime) throws DtsTransactionException {
    try {
      DtsContext.startRetryBranch(effectiveTime);
      return callback.doInTransaction();
    } catch (Throwable e) {
      throw new DtsTransactionException("Application executeRT exception", e);
    } finally {
      DtsContext.endRetryBranch();
    }
  }

}
