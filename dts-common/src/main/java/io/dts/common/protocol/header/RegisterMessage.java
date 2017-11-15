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
 * @version RegisterMessage.java, v 0.0.1 2017年9月1日 下午6:29:38 liushiming
 */
public class RegisterMessage implements CommandCustomHeader, RequestMessage {
  /**
   * 事务ID
   */
  @CFNotNull
  private long tranId;
  /**
   * 资源名称
   */
  private String clientInfo;


  public long getTranId() {
    return tranId;
  }

  public void setTranId(long tranId) {
    this.tranId = tranId;
  }

  public String getClientInfo() {
    return clientInfo;
  }

  public void setClientInfo(String dbName) {
    this.clientInfo = dbName;
  }

  @Override
  public void checkFields() throws RemotingCommandException {

  }



}
