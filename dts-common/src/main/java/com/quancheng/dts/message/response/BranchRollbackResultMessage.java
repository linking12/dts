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

import com.quancheng.dts.message.DtsMessage;

import io.netty.buffer.ByteBuf;

/**
 * @author liushiming
 * @version BranchRollbackResultMessage.java, v 0.0.1 2017年7月5日 下午5:38:00 liushiming
 * @since JDK 1.8
 */
public class BranchRollbackResultMessage extends ResultMessage {

  private static final long serialVersionUID = -8803001117670598900L;

  /**
   * 事务ID
   */
  private long tranId;

  /**
   * 分支ID
   */
  private long branchId;

  public BranchRollbackResultMessage() {
    super(256);
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_BRANCH_ROLLBACK_RESULT;
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

  @Override
  public byte[] encode() {
    super.encode();
    byteBuffer.putLong(tranId);
    byteBuffer.putLong(branchId);
    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  @Override
  public boolean decode(ByteBuf in) {
    if (!super.decode(in))
      return false;
    if (in.readableBytes() < 16)
      return false;
    this.tranId = in.readLong();
    this.branchId = in.readLong();
    return true;
  }
}
