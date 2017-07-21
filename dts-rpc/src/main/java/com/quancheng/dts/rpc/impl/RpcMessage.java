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

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author liushiming
 * @version RpcMessage.java, v 0.0.1 2017年7月21日 下午4:36:55 liushiming
 * @since JDK 1.8
 */
public class RpcMessage {
  private static AtomicLong NEXT_ID = new AtomicLong(0);

  public static long getNextMessageId() {
    return NEXT_ID.incrementAndGet();
  }

  private long id;
  private boolean isAsync;
  private boolean isRequest;
  private boolean isHeartbeat;
  private Object body;

  public boolean isAsync() {
    return isAsync;
  }

  public void setAsync(boolean isAsync) {
    this.isAsync = isAsync;
  }

  public boolean isRequest() {
    return isRequest;
  }

  public void setRequest(boolean isRequest) {
    this.isRequest = isRequest;
  }

  public boolean isHeartbeat() {
    return isHeartbeat;
  }

  public void setHeartbeat(boolean isHeartbeat) {
    this.isHeartbeat = isHeartbeat;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Object getBody() {
    return body;
  }

  public void setBody(Object body) {
    this.body = body;
  }
}
