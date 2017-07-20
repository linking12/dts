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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.AbstractFuture;

/**
 * @author liushiming
 * @version MessageFuture.java, v 0.0.1 2017年7月20日 下午1:47:07 liushiming
 * @since JDK 1.8
 */
public class MessageFuture<RpcMessage> extends AbstractFuture<RpcMessage> {

  @Override
  protected boolean set(RpcMessage resp) {
    return super.set(resp);
  }

  @Override
  protected boolean setException(Throwable throwable) {
    return super.setException(throwable);
  }


  public RpcMessage get(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException, ExecutionException {
    return super.get(timeout, unit);
  }


}
