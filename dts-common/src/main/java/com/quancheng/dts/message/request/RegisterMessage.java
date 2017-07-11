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
 * @version RegisterMessage.java, v 0.0.1 2017年7月5日 下午5:39:08 liushiming
 * @since JDK 1.8
 */
public class RegisterMessage extends DtsMessage implements MergedMessage {

  private static final long serialVersionUID = 7581303131859609405L;


  /**
   * 事务ID
   */
  private long tranId;

  /**
   * 对于缺省模式（TXC Atom为数据源），这个域为dbKey；对于MT模式，这是用户自定义key
   */
  private String key;

  /**
   * 业务主键，用于强隔离。分支上报给server，自己修改了哪些表的哪些行的主键。格式如下： "tableName1:key1,key2,key3;tableName2:key1,key2"
   */
  private String businessKey;

  /**
   * 提交模式
   */
  private byte commitMode;

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
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * @return the businessKey
   */
  public String getBusinessKey() {
    return businessKey;
  }

  /**
   * @param businessKey the businessKey to set
   */
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  /**
   * @return the commitMode
   */
  public byte getCommitMode() {
    return commitMode;
  }

  /**
   * @param commitMode the commitMode to set
   */
  public void setCommitMode(byte commitMode) {
    this.commitMode = commitMode;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_REGIST;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#encode()
   */
  @Override
  public byte[] encode() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    byteBuffer.putLong(this.tranId);
    byteBuffer.put(this.commitMode);
    if (this.key != null) {
      byte[] bs = key.getBytes(UTF8);
      byteBuffer.putShort((short) bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putShort((short) 0);

    if (this.businessKey != null) {
      byte[] bs = businessKey.getBytes(UTF8);
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
    this.commitMode = byteBuffer.get();
    short len = byteBuffer.getShort();
    if (len > 0) {
      byte[] bs = new byte[len];
      byteBuffer.get(bs);
      this.setKey(new String(bs, UTF8));
    }

    len = byteBuffer.getShort();
    if (len > 0) {
      byte[] bs = new byte[len];
      byteBuffer.get(bs);
      this.setBusinessKey(new String(bs, UTF8));
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
    return "RegisterMessage [tranId=" + tranId + ", key=" + key + ", businessKey=" + businessKey
        + ", commitMode=" + commitMode + "]";
  }

}
