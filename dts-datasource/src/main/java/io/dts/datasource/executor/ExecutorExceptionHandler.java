
package io.dts.datasource.executor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.common.exception.DtsException;

public final class ExecutorExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ExecutorExceptionHandler.class);

  private static final ThreadLocal<Boolean> IS_EXCEPTION_THROWN = new ThreadLocal<>();

  /**
   * Set throw exception if error occur or not.
   *
   * @param isExceptionThrown throw exception if error occur or not
   */
  public static void setExceptionThrown(final boolean isExceptionThrown) {
    ExecutorExceptionHandler.IS_EXCEPTION_THROWN.set(isExceptionThrown);
  }

  /**
   * Get throw exception if error occur or not.
   * 
   * @return throw exception if error occur or not
   */
  public static boolean isExceptionThrown() {
    return null == IS_EXCEPTION_THROWN.get() ? true : IS_EXCEPTION_THROWN.get();
  }

  /**
   * Handle exception.
   * 
   * @param exception to be handled exception
   */
  public static void handleException(final Exception exception) {
    if (isExceptionThrown()) {
      throw new DtsException(exception);
    }
    log.error("exception occur: ", exception);
  }
}
