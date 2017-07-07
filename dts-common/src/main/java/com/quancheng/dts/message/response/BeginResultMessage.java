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
 * @version BeginResultMessage.java, v 0.0.1 2017年7月5日 下午5:57:34 liushiming
 * @since JDK 1.8
 */
public class BeginResultMessage extends ResultMessage {

  private static final long serialVersionUID = -7218509638957782773L;

  private String xid;

  private String nextServerAddr;

  public BeginResultMessage() {
    super(256);
  }

  public String getXid() {
    return xid;
  }

  public void setXid(String xid) {
    this.xid = xid;
  }

  public String getNextServerAddr() {
    return nextServerAddr;
  }

  public void setNextServerAddr(String nextServerAddr) {
    this.nextServerAddr = nextServerAddr;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_BEGIN_RESULT;
  }

  @Override
  public byte[] encode() {
    super.encode();
    if (this.xid != null) {
      byte[] bs = xid.getBytes(UTF8);
      byteBuffer.putShort((short) bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putShort((short) 0);

    if (this.nextServerAddr != null) {
      byte[] bs = nextServerAddr.getBytes(UTF8);
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
    short len = byteBuffer.getShort();
    if (len > 0) {
      byte[] bs = new byte[len];
      byteBuffer.get(bs);
      this.setXid(new String(bs, UTF8));
    }
    len = byteBuffer.getShort();
    if (len > 0) {
      byte[] bs = new byte[len];
      byteBuffer.get(bs);
      this.setNextServerAddr(new String(bs, UTF8));
    }
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "BeginResultMessage [xid=" + xid + "]";
  }

}
