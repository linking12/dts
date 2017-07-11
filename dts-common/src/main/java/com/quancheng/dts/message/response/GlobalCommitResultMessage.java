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
 * @version GlobalCommitResultMessage.java, v 0.0.1 2017年7月11日 下午3:43:25 liushiming
 * @since JDK 1.8
 */
public class GlobalCommitResultMessage extends ResultMessage {

  private static final long serialVersionUID = -154378672025418855L;

  private long tranId;

  public GlobalCommitResultMessage() {
    super(256);
  }

  public long getTranId() {
    return tranId;
  }

  public void setTranId(long tranId) {
    this.tranId = tranId;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_BRANCH_ROLLBACK_RESULT;
  }

  @Override
  public byte[] encode() {
    super.encode();
    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  @Override
  public void decode(ByteBuffer byteBuffer) {
    super.decode(byteBuffer);
  }

}
