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
 * @version ReportStatusResultMessage.java, v 0.0.1 2017年7月13日 上午10:30:57 liushiming
 * @since JDK 1.8
 */
public class ReportStatusResultMessage extends ResultMessage {

  private static final long serialVersionUID = 7794118296024718436L;

  private long branchId;


  public ReportStatusResultMessage() {
    super(256);
  }

  public long getBranchId() {
    return branchId;
  }

  public void setBranchId(long branchId) {
    this.branchId = branchId;
  }


  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_REPORT_STATUS_RESULT;
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
    return "ReportStatusResultMessage [branchId=" + branchId + "]";
  }

}
