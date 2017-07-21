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
package com.quancheng.dts.cluster;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liushiming
 * @version AbstractVirtualGroupAddressManager.java, v 0.0.1 2017年7月21日 下午3:50:27 liushiming
 * @since JDK 1.8
 */
public abstract class AbstractVirtualGroupAddressManager implements AddressManager {

  private static final Logger logger =
      LoggerFactory.getLogger(AbstractVirtualGroupAddressManager.class);

  private final static String VGROUP_MAPPING_DATAID_PREFIX = "vgroup_mapping_";

  @Override
  public void getAddressList(String vgroup, AddressWatcher watcher) throws InterruptedException {

  }

  public abstract void getAddressListFromStorage(String rGroup) throws InterruptedException;

  public abstract void getAddressListFromStorage(String rGroup, AddressWatcher watcher)
      throws InterruptedException;

  public abstract class MyObserver<T> {
    protected CountDownLatch countDownLatch;
    protected AddressWatcher watcher;
    protected boolean disable = false;

    /**
     * @param countDownLatch
     * @param watcher
     */
    protected MyObserver(CountDownLatch countDownLatch, AddressWatcher watcher) {
      super();
      this.countDownLatch = countDownLatch;
      this.watcher = watcher;
    }

    protected synchronized void dateChange(T data) {
      if (disable)
        return;
      watcher.onAddressListChanged(toAddressList(data));
      if (countDownLatch.getCount() == 1) {
        countDownLatch.countDown();
      }
    }

    protected synchronized void disable() {
      disable = true;
    }

    protected abstract List<String> toAddressList(T date);
  }

}
