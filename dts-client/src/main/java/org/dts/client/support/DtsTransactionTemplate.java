package org.dts.client.support;

import org.dts.client.DtsTransactionManager;
import org.dts.client.exception.DtsTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
