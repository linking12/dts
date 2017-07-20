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
package com.quancheng.dts.rpc.impl;

import java.nio.ByteBuffer;

import com.quancheng.dts.message.DtsMessage;

import io.netty.buffer.ByteBuf;

/**
 * @author liushiming
 * @version RegisterClientAppNameMessage.java, v 0.0.1 2017年7月20日 下午2:48:13 liushiming
 * @since JDK 1.8
 */
public class RegisterClientAppNameMessage extends DtsMessage {

  private static final long serialVersionUID = -6694705262336061204L;

  private String clientAppName;

  private String version = "1.0.0";

  private ByteBuffer byteBuffer;

  public RegisterClientAppNameMessage() {
    this(256);
  }

  public RegisterClientAppNameMessage(int size) {
    super();
    this.byteBuffer = ByteBuffer.allocate(size);
  }


  public String getClientAppName() {
    return clientAppName;
  }

  public void setClientAppName(String clientAppName) {
    this.clientAppName = clientAppName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_REG_CLT;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#encode()
   */
  @Override
  public byte[] encode() {
    if (this.clientAppName != null) {
      byte[] bs = clientAppName.getBytes(UTF8);
      byteBuffer.putShort((short) bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putShort((short) 0);

    if (this.version != null) {
      byte[] bs = version.getBytes(UTF8);
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
  public boolean decode(ByteBuf in) {
    int i = in.readableBytes();
    if (i < 4)
      return false;
    i -= 4;

    short len = in.readShort();
    if (len > 0) {
      if (i < len)
        return false;
      else
        i -= len;
      byte[] bs = new byte[len];
      in.readBytes(bs);
      this.setClientAppName(new String(bs, UTF8));
    }

    len = in.readShort();
    if (len > 0) {
      if (i < len)
        return false;

      byte[] bs = new byte[len];
      in.readBytes(bs);
      this.setVersion(new String(bs, UTF8));
    }

    return true;
  }
}
