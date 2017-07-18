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

import com.quancheng.dts.message.DtsMessage;

import io.netty.buffer.ByteBuf;

/**
 * @author liushiming
 * @version ClusterDumpResultMessage.java, v 0.0.1 2017年7月18日 下午3:19:25 liushiming
 * @since JDK 1.8
 */
public class ClusterDumpResultMessage extends DtsMessage {

  private static final long serialVersionUID = -4566761304528956910L;

  private boolean result;

  private String msg;

  public ByteBuffer byteBuffer = ByteBuffer.allocate(32 * 1024 * 1024);

  public boolean isResult() {
    return result;
  }

  public void setResult(boolean result) {
    this.result = result;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @Override
  public short getTypeCode() {
    return TYPE_CLUSTER_DUMP_RESULT;
  }

  @Override
  public String toString() {
    return "ClusterDumpResultMessage msg:" + msg;
  }

  @Override
  public byte[] encode() {
    byteBuffer.put(result ? (byte) 1 : (byte) 0);
    if (this.msg != null) {
      byte[] bs = msg.getBytes(UTF8);
      byteBuffer.putInt(bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putInt(0);

    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  @Override
  public boolean decode(ByteBuf in) {
    int i = in.readableBytes();
    if (i < 3)
      return false;
    else
      i -= 3;
    this.result = in.readBoolean();
    int len = in.readInt();
    if (len > 0) {
      if (i < len)
        return false;
      else
        i -= len;
      byte[] bs = new byte[len];
      in.readBytes(bs);
      this.setMsg(new String(bs, UTF8));
    }
    return true;
  }
}
