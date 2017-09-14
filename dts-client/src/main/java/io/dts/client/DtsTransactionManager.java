package io.dts.client;

import io.dts.client.exception.DtsTransactionException;

/**
 * Created by guoyubo on 2017/8/24.
 */
public interface DtsTransactionManager {

  void begin(long timeout) throws DtsTransactionException;

  void commit() throws DtsTransactionException;

  void commit(int retryTimes) throws DtsTransactionException;

  void rollback() throws DtsTransactionException;

  void rollback(int retryTimes) throws DtsTransactionException;

}
