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
 * @version RegisterResultMessage.java, v 0.0.1 2017年7月11日 下午4:56:01 liushiming
 * @since JDK 1.8
 */
public class RegisterResultMessage extends ResultMessage {


  private static final long serialVersionUID = -8615432904445833868L;
  /**
   * 事务ID
   */
  private long tranId;

  /**
   * 分支ID
   */
  private long branchId;

  public RegisterResultMessage() {
    super(256);
  }

  /**
   * @return the tranId
   */
  public long getTranId() {
    return tranId;
  }

  /**
   * @param tranId the tranId to set
   */
  public void setTranId(long tranId) {
    this.tranId = tranId;
  }



  /**
   * @return the branchId
   */
  public long getBranchId() {
    return branchId;
  }



  /**
   * @param branchId the branchId to set
   */
  public void setBranchId(long branchId) {
    this.branchId = branchId;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_REGIST_RESULT;
  }

  @Override
  public byte[] encode() {
    super.encode();
    byteBuffer.putLong(tranId);
    byteBuffer.putLong(branchId);

    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  @Override
  public void decode(ByteBuffer byteBuffer) {
    super.decode(byteBuffer);
    this.tranId = byteBuffer.getLong();
    this.branchId = byteBuffer.getLong();
  }
}
