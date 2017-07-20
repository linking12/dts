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

import java.io.Serializable;

/**
 * @author liushiming
 * @version HeartbeatMessage.java, v 0.0.1 2017年7月20日 下午1:44:17 liushiming
 * @since JDK 1.8
 */
public class HeartbeatMessage implements Serializable {
  private static final long serialVersionUID = -985316399527884899L;

  private HeartbeatMessage() {}

  public static HeartbeatMessage PING = new HeartbeatMessage();
  public static HeartbeatMessage PONG = new HeartbeatMessage();
}
