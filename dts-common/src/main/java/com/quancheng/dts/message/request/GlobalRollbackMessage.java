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
 * @author liushiming
 * @version GlobalRollbackMessage.java, v 0.0.1 2017年7月5日 下午5:38:56 liushiming
 * @since JDK 1.8
 */
public class GlobalRollbackMessage extends DtsMessage implements MergedMessage {

  private static final long serialVersionUID = -8946496607908804418L;
  /**
   * 事务ID
   */
  private long tranId;

  /**
   * 当server采用cluster mode，有可能消息接收svr并不是发起事务的svr，这个属性指示发起事务的svr地址
   */
  private String realSvrAddr;


  private int retryTimes;

  public long getTranId() {
    return tranId;
  }


  public void setTranId(long tranId) {
    this.tranId = tranId;
  }

  public String getRealSvrAddr() {
    return realSvrAddr;
  }


  public void setRealSvrAddr(String realSvrAddr) {
    this.realSvrAddr = realSvrAddr;
  }

  public int getRetryTimes() {
    return retryTimes;
  }

  public void setRetryTimes(final int retryTimes) {
    this.retryTimes = retryTimes;
  }

  @Override
  public short getTypeCode() {
    return TYPE_GLOBAL_ROLLBACK;
  }

  @Override
  public byte[] encode() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(256);
    byteBuffer.putLong(this.tranId);
    if (this.realSvrAddr != null) {
      byte[] bs = realSvrAddr.getBytes(UTF8);
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

  /**
   * @see com.quancheng.dts.message.MergedMessage#decode(java.nio.ByteBuffer)
   */
  @Override
  public void decode(ByteBuffer byteBuffer) {
    this.tranId = byteBuffer.getLong();
    short len = byteBuffer.getShort();
    if (len > 0) {
      byte[] bs = new byte[len];
      byteBuffer.get(bs);
      this.setRealSvrAddr(new String(bs, UTF8));
    }
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
    return "GlobalRollbackMessage [tranId=" + tranId + ", realSvrAddr=" + realSvrAddr + "]";
  }


}
