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

import io.netty.buffer.ByteBuf;

/**
 * @author liushiming
 * @version ClusterDumpMessage.java, v 0.0.1 2017年7月18日 下午3:19:01 liushiming
 * @since JDK 1.8
 */
public class ClusterDumpMessage extends DtsMessage {
  private static final long serialVersionUID = -6826254198463287830L;
  private boolean verbose;

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  @Override
  public short getTypeCode() {
    return TYPE_CLUSTER_DUMP;
  }

  @Override
  public byte[] encode() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(16);
    byteBuffer.put(verbose ? (byte) 1 : (byte) 0);
    byteBuffer.flip();
    byte[] content = new byte[byteBuffer.limit()];
    byteBuffer.get(content);
    return content;
  }

  @Override
  public boolean decode(ByteBuf in) {
    if (in.readableBytes() < 1)
      return false;

    this.verbose = (in.readByte() == 1);
    return true;
  }
}
