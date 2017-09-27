package io.dts.client.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.reflect.UndeclaredThrowableException;

import io.dts.client.exception.DtsTransactionException;
import io.dts.client.DtsTransactionManager;

/**
 * Created by guoyubo on 2017/8/28.
 */
public class DtsATTransactionTemplate implements DtsTransactionOperations {

  private static final Logger logger = LoggerFactory.getLogger(DtsATTransactionTemplate.class);

  private DtsTransactionManager transactionManager;

  public DtsATTransactionTemplate(final DtsTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  @Override
  public <T> T execute(DtsTransactionCallback<T> action, long timeout) throws DtsTransactionException {

    T result = null;
    try {
      this.transactionManager.begin(timeout);
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


  private void rollbackOnException(Throwable ex) throws DtsTransactionException {
    logger.debug("Initiating transaction rollback on application exception", ex);
    this.transactionManager.rollback();
  }

}
