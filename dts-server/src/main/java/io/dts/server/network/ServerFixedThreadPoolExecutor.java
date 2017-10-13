/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.dts.server.network;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liushiming
 * @version ServerFixedThreadPoolExecutor.java, v 0.0.1 2017年9月12日 下午6:42:31 liushiming
 */
public class ServerFixedThreadPoolExecutor extends ThreadPoolExecutor {
  public ServerFixedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
      final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  public ServerFixedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
      final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue,
      final ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
  }

  public ServerFixedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
      final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue,
      final RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
  }

  public ServerFixedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
      final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue,
      final ThreadFactory threadFactory, final RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
  }

  @Override
  protected <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {
    return new FutureTaskExt<T>(runnable, value);
  }
}
