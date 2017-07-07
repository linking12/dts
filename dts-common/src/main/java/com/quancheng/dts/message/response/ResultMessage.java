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
import com.quancheng.dts.message.MergedMessage;

import io.netty.buffer.ByteBuf;

/**
 * @author liushiming
 * @version ResultMessage.java, v 0.0.1 2017年7月5日 下午5:08:50 liushiming
 * @since JDK 1.8
 */
public abstract class ResultMessage extends DtsMessage implements MergedMessage {

  private static final long serialVersionUID = 8921411043866254260L;

  protected final ByteBuffer byteBuffer;

  private int result;

  private String msg;

  public ResultMessage(int size) {
    this.byteBuffer = ByteBuffer.allocate(size);
  }

  public int getResult() {
    return result;
  }

  public void setResult(int result) {
    this.result = result;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @Override
  public byte[] encode() {
    byteBuffer.put((byte) result);
    if (result != 1) {
      if (getMsg() != null) {
        byte[] bs = getMsg().getBytes(UTF8);
        byteBuffer.putShort((short) bs.length);
        if (bs.length > 0)
          byteBuffer.put(bs);
      } else
        byteBuffer.putShort((short) 0);
    }

    return null;
  }

  @Override
  public void decode(ByteBuffer byteBuffer) {
    this.setResult(byteBuffer.get());
    if (result != 1) {
      short len = byteBuffer.getShort();
      if (len > 0) {
        byte[] msg = new byte[len];
        byteBuffer.get(msg);
        this.setMsg(new String(msg, UTF8));
      }
    }
  }

  @Override
  public boolean decode(ByteBuf in) {
    int i = in.readableBytes();
    if (i < 1)
      return false;
    this.setResult(in.readByte());
    i--;
    if (result != 1) {
      if (i < 2)
        return false;

      short len = in.readShort();
      i -= 2;
      if (i < len)
        return false;

      if (len > 0) {
        byte[] msg = new byte[len];
        in.readBytes(msg);
        this.setMsg(new String(msg, UTF8));
      }
    }

    return true;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ResultMessage [result=" + result + ", msg=" + msg + "]";
  }

}
