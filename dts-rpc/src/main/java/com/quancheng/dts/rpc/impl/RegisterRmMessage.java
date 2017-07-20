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
 * @version RegisterRmMessage.java, v 0.0.1 2017年7月20日 下午2:48:37 liushiming
 * @since JDK 1.8
 */
public class RegisterRmMessage extends DtsMessage {

  private static final long serialVersionUID = 8839215354541487424L;

  private String dbKeys;

  private String version = "1.0.0";

  private short type = 0;

  private String appName;

  public ByteBuffer byteBuffer;

  public RegisterRmMessage() {
    this(256);
  }

  public RegisterRmMessage(int size) {
    super();
    this.byteBuffer = ByteBuffer.allocate(size);
  }


  public String getDbKeys() {
    return dbKeys;
  }

  public void setDbKeys(String dbKeys) {
    this.dbKeys = dbKeys;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public short getType() {
    return type;
  }

  public void setType(short type) {
    this.type = type;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_REG_RM;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#encode()
   */
  @Override
  public byte[] encode() {
    if (this.dbKeys != null) {
      byte[] bs = dbKeys.getBytes(UTF8);
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

    if (this.version != null) {
      byteBuffer.putShort(type);
      if (this.appName != null) {
        byte[] bs = appName.getBytes(UTF8);
        byteBuffer.putShort((short) bs.length);
        if (bs.length > 0)
          byteBuffer.put(bs);
      } else
        byteBuffer.putShort((short) 0);
    }

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
      this.setDbKeys(new String(bs, UTF8));
    }

    len = in.readShort();
    if (len > 0) {
      if (i < len)
        return false;
      else
        i -= len;

      byte[] bs = new byte[len];
      in.readBytes(bs);
      this.setVersion(new String(bs, UTF8));
    }

    if (this.version != null) {
      if (i < 4)
        return false;
      i -= 4;
      type = in.readShort();
      len = in.readShort();
      if (len > 0) {
        if (i < len)
          return false;
        else
          i -= len;

        byte[] bs = new byte[len];
        in.readBytes(bs);
        this.setAppName(new String(bs, UTF8));
      }
    }

    return true;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "RegisterRmMessage [dbKeys=" + dbKeys + ", version=" + version + ", type=" + type
        + ", appName=" + appName + "]";
  }

}
