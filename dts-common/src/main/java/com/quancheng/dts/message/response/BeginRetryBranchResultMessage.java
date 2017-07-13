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
 * @version BeginRetryBranchResultMessage.java, v 0.0.1 2017年7月13日 下午4:19:02 liushiming
 * @since JDK 1.8
 */
public class BeginRetryBranchResultMessage extends ResultMessage {

  private static final long serialVersionUID = 3052600211780138619L;

  /**
   * 事务XID
   */
  private String xid;

  /**
   * 分支ID
   */
  private long branchId;

  public BeginRetryBranchResultMessage() {
    super(256);
  }


  public String getXid() {
    return xid;
  }


  public void setXid(String xid) {
    this.xid = xid;
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
    return TYPE_BEGIN_RETRY_BRANCH_RESULT;
  }

  @Override
  public byte[] encode() {
    super.encode();
    byteBuffer.putLong(branchId);
    if (this.xid != null) {
      byte[] bs = xid.getBytes(UTF8);
      byteBuffer.putShort((short) bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putShort((short) 0);

    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  @Override
  public void decode(ByteBuffer byteBuffer) {
    super.decode(byteBuffer);
    this.branchId = byteBuffer.getLong();
    short len = byteBuffer.getShort();
    if (len > 0) {
      byte[] msg = new byte[len];
      byteBuffer.get(msg);
      this.setXid(new String(msg, UTF8));
    }
  }


  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "BeginRetryBranchResultMessage [xid=" + xid + ", branchId=" + branchId + "]";
  }
}
