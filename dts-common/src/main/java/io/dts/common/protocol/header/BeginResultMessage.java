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

import io.dts.common.protocol.ResponseMessage;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.annotation.CFNotNull;
import io.dts.remoting.exception.RemotingCommandException;

/**
 * 开始事务的返回消息
 * 
 * @author liushiming
 * @version BeginResultMessage.java, v 0.0.1 2017年9月1日 下午6:32:09 liushiming
 */
public class BeginResultMessage implements CommandCustomHeader, ResponseMessage {
  /**
   * 全局唯一的事务ID
   */
  @CFNotNull
  private String xid;
  /**
   * Server双写配置下，next node的地址
   */
  @CFNotNull
  private String nextSvrAddr;

  public String getXid() {
    return xid;
  }

  public void setXid(String xid) {
    this.xid = xid;
  }

  public String getNextSvrAddr() {
    return nextSvrAddr;
  }

  public void setNextSvrAddr(String nextSvrAddr) {
    this.nextSvrAddr = nextSvrAddr;
  }

  @Override
  public void checkFields() throws RemotingCommandException {

  }


}
