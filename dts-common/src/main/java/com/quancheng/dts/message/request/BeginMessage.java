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
package com.quancheng.dts.message.request;

import java.nio.ByteBuffer;

import com.quancheng.dts.message.DtsMessage;
import com.quancheng.dts.message.MergedMessage;
import com.quancheng.dts.message.response.ResultMessage;

/**
 * 开启事务消息
 * 
 * @author liushiming
 * @version BeginMessage.java, v 0.0.1 2017年7月5日 下午5:28:24 liushiming
 * @since JDK 1.8
 */
public class BeginMessage extends DtsMessage implements MergedMessage {

  private static final long serialVersionUID = 249294228243228918L;

  private long timeout = 60000;

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * 
   * @see com.quancheng.dts.message.DtsMessage#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_BEGIN;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#encode()
   */
  @Override
  public byte[] encode() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(16);
    byteBuffer.putLong(timeout);
    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  /**
   * @see com.quancheng.dts.message.MergedMessage#decode(java.nio.ByteBuffer)
   */
  @Override
  public void decode(ByteBuffer byteBuffer) {
    this.timeout = byteBuffer.getLong();
  }

  /**
   * @see com.quancheng.dts.message.DtsMessage#handleMessage(long, java.lang.String,
   *      java.lang.String, java.lang.String, com.quancheng.dts.message.DtsMessage,
   *      com.quancheng.dts.message.response.ResultMessage[], int)
   */
  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      DtsMessage message, ResultMessage[] results, int idx) {
    super.getHandler().handleMessage(msgId, dbKeys, clientIp, clientAppName, this, results, idx);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "BeginMessage [timeout=" + timeout + "]";
  }
}
