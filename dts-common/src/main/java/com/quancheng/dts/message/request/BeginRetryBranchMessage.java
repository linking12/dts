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
 * @version BeginRetryBranchMessage.java, v 0.0.1 2017年7月5日 下午5:39:28 liushiming
 * @since JDK 1.8
 */
public class BeginRetryBranchMessage extends DtsMessage implements MergedMessage {

  private static final long serialVersionUID = -8275936907012036078L;

  /**
   * 有效时长；超出这个时长，转为告警通知用户。 缺省半小时
   */
  private long effectiveTime = 30 * 60000;

  /**
   * 完整DB name串
   */
  private String dbName;

  /**
   * 提交模式
   */
  private byte commitMode;

  /**
   * 重试的SQL
   */
  private String sql;



  public long getEffectiveTime() {
    return effectiveTime;
  }


  public void setEffectiveTime(long effectiveTime) {
    this.effectiveTime = effectiveTime;
  }


  public String getDbName() {
    return dbName;
  }


  public void setDbName(String dbName) {
    this.dbName = dbName;
  }


  public byte getCommitMode() {
    return commitMode;
  }


  public void setCommitMode(byte commitMode) {
    this.commitMode = commitMode;
  }


  public String getSql() {
    return sql;
  }


  public void setSql(String sql) {
    this.sql = sql;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_BEGIN_RETRY_BRANCH;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#encode()
   */
  @Override
  public byte[] encode() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
    byteBuffer.putLong(this.effectiveTime);
    byteBuffer.put(this.commitMode);
    if (this.dbName != null) {
      byte[] bs = dbName.getBytes(UTF8);
      byteBuffer.putShort((short) bs.length);
      if (bs.length > 0)
        byteBuffer.put(bs);
    } else
      byteBuffer.putShort((short) 0);

    if (this.sql != null) {
      byte[] bs = sql.getBytes(UTF8);
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
    this.effectiveTime = byteBuffer.getLong();
    this.commitMode = byteBuffer.get();
    short len = byteBuffer.getShort();
    if (len > 0) {
      byte[] bs = new byte[len];
      byteBuffer.get(bs);
      this.setDbName(new String(bs, UTF8));
    }

    len = byteBuffer.getShort();
    if (len > 0) {
      byte[] bs = new byte[len];
      byteBuffer.get(bs);
      this.setSql(new String(bs, UTF8));
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
    return "BeginRetryBranchMessage [effectiveTime=" + effectiveTime + ", dbName=" + dbName
        + ", commitMode=" + commitMode + ", sql=" + sql + "]";
  }

}
