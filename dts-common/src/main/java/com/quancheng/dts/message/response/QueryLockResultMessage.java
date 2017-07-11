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
package com.quancheng.dts.message.response;

import java.nio.ByteBuffer;

/**
 * @author liushiming
 * @version QueryLockResultMessage.java, v 0.0.1 2017年7月11日 下午4:41:06 liushiming
 * @since JDK 1.8
 */
public class QueryLockResultMessage extends ResultMessage {

  private static final long serialVersionUID = -1959924099229800122L;
  /**
   * 事务ID
   */
  private long tranId;

  /**
   * 业务主键，用于强隔离。分支上报给server，自己修改了哪些表的哪些行的主键。格式如下： "tableName1:key1,key2,key3;tableName2:key1,key2"
   */
  private String businessKey;

  public QueryLockResultMessage() {
    super(256);
  }

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

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_QUERY_LOCK_RESULT;
  }

  @Override
  public byte[] encode() {
    super.encode();
    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  @Override
  public void decode(ByteBuffer byteBuffer) {
    super.decode(byteBuffer);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "QueryLockResultMessage [tranId=" + tranId + ", businessKey=" + businessKey + "]";
  }


}
