package com.quancheng.dts.event.eventbus;

import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncEventBus implements EventBus {
  private static final Integer DEFAULT_THREAD_POOL_CORE_SIZE = 1;
  private static final Integer DEFAULT_THREAD_POOL_MAX_SIZE = 10;

  private final com.google.common.eventbus.AsyncEventBus asyncEventBus;

  public AsyncEventBus() {
    this.asyncEventBus = new com.google.common.eventbus.AsyncEventBus(
        new ThreadPoolExecutor(DEFAULT_THREAD_POOL_CORE_SIZE, DEFAULT_THREAD_POOL_MAX_SIZE,
            60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
  }

  /**
   * Constructor.
   *
   * @param executor the executor used to execute event listener.
   */
  public AsyncEventBus(Executor executor) {
    this.asyncEventBus = new com.google.common.eventbus.AsyncEventBus(executor);
  }

  @Override
  public <T> void register(EventListener<T> listener) {
    this.asyncEventBus.register(listener);
  }

  @Override
  public <T> void post(T event) {
    asyncEventBus.post(event);
  }

}
