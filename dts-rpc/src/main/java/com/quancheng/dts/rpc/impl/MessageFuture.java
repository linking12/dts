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
package com.quancheng.dts.rpc.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author liushiming
 * @version MessageFuture.java, v 0.0.1 2017年7月20日 下午1:47:07 liushiming
 * @since JDK 1.8
 */
public class MessageFuture {

  private RpcMessage requestMessage;
  private long timeout;
  private long start = System.currentTimeMillis();
  private volatile Object resultMessage;
  private static final Object NULL = new Object();
  private final CountDownLatch latch = new CountDownLatch(1);

  public boolean isTimeout() {
    return System.currentTimeMillis() - start > timeout;
  }

  public Object get(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
    boolean success = latch.await(timeout, unit);
    if (!success) {
      throw new TimeoutException("cost " + (System.currentTimeMillis() - start) + " ms");
    }

    if (resultMessage instanceof RuntimeException) {
      throw (RuntimeException) resultMessage;
    } else if (resultMessage instanceof Throwable) {
      throw new RuntimeException((Throwable) resultMessage);
    }

    return resultMessage;
  }

  public void setResultMessage(Object obj) {
    this.resultMessage = (obj == null ? NULL : obj);
    latch.countDown();
  }

  public RpcMessage getRequestMessage() {
    return requestMessage;
  }

  public void setRequestMessage(RpcMessage requestMessage) {
    this.requestMessage = requestMessage;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

}
