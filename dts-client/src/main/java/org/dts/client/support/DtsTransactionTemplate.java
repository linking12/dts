package org.dts.client.support;

import org.dts.client.DtsTransactionManager;
import org.dts.client.exception.DtsTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.common.DtsContext;
import com.quancheng.dts.exception.DtsException;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * Created by guoyubo on 2017/8/28.
 */
public class DtsTransactionTemplate implements DtsTransactionOperations {

  private static final Logger logger = LoggerFactory.getLogger(DtsTransactionTemplate.class);

  private DtsTransactionManager transactionManager;

  public DtsTransactionTemplate(final DtsTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  @Override
  public <T> T execute(DtsTransactionCallback<T> action) throws DtsTransactionException {

    T result = null;
    try {
      this.transactionManager.begin(3000L);
      result = action.doInTransaction();
      this.transactionManager.commit();
    } catch (RuntimeException ex) {
        // Transactional code threw application exception -> rollback
        rollbackOnException(ex);
    }
    catch (Error err) {
      // Transactional code threw error -> rollback
      rollbackOnException(err);
    }
    catch (Throwable ex) {
      // Transactional code threw unexpected exception -> rollback
      throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
    }
    return result;
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
  public <T> T executeRT(DtsTransactionCallback<T> callback, long effectiveTime) throws DtsTransactionException {
    try {
      DtsContext.startRetryBranch(effectiveTime);
      return callback.doInTransaction();
    } catch (Throwable e) {
      throw new DtsTransactionException("Application executeRT exception", e);
    } finally {
      DtsContext.endRetryBranch();
    }
  }

  private void rollbackOnException(Throwable ex) throws DtsTransactionException {
    logger.debug("Initiating transaction rollback on application exception", ex);
    try {
      this.transactionManager.rollback();
    }
    catch (DtsException ex2) {
      logger.error("Application exception overridden by rollback exception", ex);
      throw new DtsTransactionException("Application exception overridden by rollback exception", ex2);
    }
    catch (RuntimeException ex2) {
      logger.error("Application exception overridden by rollback exception", ex);
      throw new DtsTransactionException("Application exception overridden by rollback exception", ex2);
    }
    catch (Error err) {
      logger.error("Application exception overridden by rollback error", ex);
      throw err;
    }
  }
}
