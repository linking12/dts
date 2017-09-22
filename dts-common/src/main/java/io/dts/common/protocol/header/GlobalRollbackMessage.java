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
 * @version GlobalRollbackMessage.java, v 0.0.1 2017年9月1日 下午6:27:32 liushiming
 */
public class GlobalRollbackMessage implements CommandCustomHeader, RequestMessage {
  /**
   * 事务ID
   */
  @CFNotNull
  private long tranId;

  /**
   * 当server采用cluster mode，有可能消息接收svr并不是发起事务的svr，这个属性指示发起事务的svr地址
   */
  private String realSvrAddr;

  public long getTranId() {
    return tranId;
  }

  public void setTranId(long tranId) {
    this.tranId = tranId;
  }

  public String getRealSvrAddr() {
    return realSvrAddr;
  }

  public void setRealSvrAddr(String realSvrAddr) {
    this.realSvrAddr = realSvrAddr;
  }


  @Override
  public void checkFields() throws RemotingCommandException {

  }



}
