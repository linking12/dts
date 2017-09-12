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

import io.dts.common.protocol.RequestHeaderMessage;
import io.dts.remoting.exception.RemotingCommandException;

/**
 * @author liushiming
 * @version QueryLockMessage.java, v 0.0.1 2017年9月1日 下午6:25:41 liushiming
 */
public class QueryLockMessage implements RequestHeaderMessage {

  /**
   * 事务ID
   */
  private long tranId;

  /**
   * 业务主键，用于强隔离。分支上报给server，自己修改了哪些表的哪些行的主键。格式如下： "tableName1:key1,key2,key3;tableName2:key1,key2"
   */
  private String businessKey;

  public long getTranId() {
    return tranId;
  }

  public void setTranId(long tranId) {
    this.tranId = tranId;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }


  @Override
  public short getTypeCode() {
    return TYPE_QUERY_LOCK;
  }

  @Override
  public void checkFields() throws RemotingCommandException {
    // TODO Auto-generated method stub

  }


}
