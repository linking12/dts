package org.dts.client.support;

/**
 * Created by guoyubo on 2017/8/28.
 */
public interface DtsTransactionCallback<T> {

  T doInTransaction() throws Throwable;
}
