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
package io.dts.common.lifecycle;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liushiming
 * @version AbstractLifecycleComponent.java, v 0.0.1 2017年9月13日 下午1:49:33 liushiming
 */
public abstract class AbstractLifecycleComponent implements LifecycleComponent {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected final Lifecycle lifecycle = new Lifecycle();

  private final List<LifecycleListener> listeners = new CopyOnWriteArrayList<>();

  @Override
  public Lifecycle.State lifecycleState() {
    return this.lifecycle.state();
  }

  @Override
  public void addLifecycleListener(LifecycleListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeLifecycleListener(LifecycleListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void start() {
    if (!lifecycle.canMoveToStarted()) {
      return;
    }
    for (LifecycleListener listener : listeners) {
      listener.beforeStart();
    }
    doStart();
    lifecycle.moveToStarted();
    for (LifecycleListener listener : listeners) {
      listener.afterStart();
    }
  }

  protected abstract void doStart();

  @Override
  public void stop() {
    if (!lifecycle.canMoveToStopped()) {
      return;
    }
    for (LifecycleListener listener : listeners) {
      listener.beforeStop();
    }
    lifecycle.moveToStopped();
    doStop();
    for (LifecycleListener listener : listeners) {
      listener.afterStop();
    }
  }

  protected abstract void doStop();

  @Override
  public void close() {
    if (lifecycle.started()) {
      stop();
    }
    if (!lifecycle.canMoveToClosed()) {
      return;
    }
    for (LifecycleListener listener : listeners) {
      listener.beforeClose();
    }
    lifecycle.moveToClosed();
    try {
      doClose();
    } catch (IOException e) {
      logger.warn("failed to close " + getClass().getName(), e);
    }
    for (LifecycleListener listener : listeners) {
      listener.afterClose();
    }
  }

  protected abstract void doClose() throws IOException;
}
