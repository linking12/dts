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
package com.quancheng.dts.rpc;

/**
 * 
 * @author liushiming
 * @version ConnectionMessage.java, v 0.0.1 2017年7月18日 下午4:06:11 liushiming
 * @since JDK 1.8
 */
public class ConnectionMessage {

  private String clientIp;

  private String clientAppName;

  private String dbKeys;

  private boolean connected;

  private String clientIpAndPort;

  public boolean isConnected() {
    return connected;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public String getClientIp() {
    return clientIp;
  }

  public void setClientIp(String clientIp) {
    this.clientIp = clientIp;
  }

  public String getClientAppName() {
    return clientAppName;
  }

  public void setClientAppName(String clientAppName) {
    this.clientAppName = clientAppName;
  }

  public String getDbKeys() {
    return dbKeys;
  }

  public void setDbKeys(String dbKeys) {
    this.dbKeys = dbKeys;
  }

  public String getClientIpAndPort() {
    return clientIpAndPort;
  }

  public void setClientIpAndPort(String clientIpAndPort) {
    this.clientIpAndPort = clientIpAndPort;
  }

  public String toString() {
    return "ConnectionMessage Client IP:" + this.clientIp + ",Client App:" + this.clientAppName
        + ",dbKeys:" + this.dbKeys + ",connected:" + this.connected;
  }

  public static ConnectionMessage CLIENT_CONNECTED = new ConnectionMessage();
  public static ConnectionMessage CLIENT_DISCONNECTED = new ConnectionMessage();
}
