package io.dts.client.api;

import io.dts.common.exception.DtsException;

/**
 * Created by guoyubo on 2017/8/24.
 */
public interface DtsTransactionManager {

  void begin(long timeout) throws DtsException;

  void commit() throws DtsException;

  void commit(int retryTimes) throws DtsException;

  void rollback() throws DtsException;

  void rollback(int retryTimes) throws DtsException;

}
