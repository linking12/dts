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

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author liushiming
 * @version FutureTaskExt.java, v 0.0.1 2017年9月12日 下午6:43:30 liushiming
 */
public class FutureTaskExt<V> extends FutureTask<V> {
  private final Runnable runnable;

  public FutureTaskExt(final Callable<V> callable) {
    super(callable);
    this.runnable = null;
  }

  public FutureTaskExt(final Runnable runnable, final V result) {
    super(runnable, result);
    this.runnable = runnable;
  }

  public Runnable getRunnable() {
    return runnable;
  }
}
