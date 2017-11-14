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
package io.dts.common.protocol.header;

import io.dts.common.protocol.RequestMessage;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.annotation.CFNotNull;
import io.dts.remoting.exception.RemotingCommandException;

/**
 * @author liushiming
 * @version BranchRollBackMessage.java, v 0.0.1 2017年9月1日 下午5:47:51 liushiming
 */
public class BranchRollBackMessage implements CommandCustomHeader, RequestMessage {
  /**
   * 服务端地址
   */
  private String serverAddr;
  /**
   * 事务ID
   */
  @CFNotNull
  private long tranId;
  /**
   * 分支ID
   */
  @CFNotNull
  private long branchId;

  private String appName;

  private String dbName;

  private String udata = null;

  public String getServerAddr() {
    return serverAddr;
  }

  public void setServerAddr(String serverAddr) {
    this.serverAddr = serverAddr;
  }

  public long getTranId() {
    return tranId;
  }

  public void setTranId(long tranId) {
    this.tranId = tranId;
  }

  public long getBranchId() {
    return branchId;
  }

  public void setBranchId(long branchId) {
    this.branchId = branchId;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public String getUdata() {
    return udata;
  }

  public void setUdata(String udata) {
    this.udata = udata;
  }



  @Override
  public void checkFields() throws RemotingCommandException {

  }



}
