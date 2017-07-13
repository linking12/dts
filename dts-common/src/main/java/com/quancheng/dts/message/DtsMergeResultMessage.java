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
package com.quancheng.dts.message;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.message.response.ResultMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author liushiming
 * @version DtsMergeResultMessage.java, v 0.0.1 2017年7月13日 下午4:20:05 liushiming
 * @since JDK 1.8
 */
public class DtsMergeResultMessage extends ResultMessage implements MergeMessage {

  private static final long serialVersionUID = 8386815271362483836L;

  private static final Logger logger = LoggerFactory.getLogger(DtsMergeResultMessage.class);

  public ResultMessage[] msgs;

  public DtsMergeResultMessage() {
    super(256);
  }

  public ResultMessage[] getMsgs() {
    return msgs;
  }

  public void setMsgs(ResultMessage[] msgs) {
    this.msgs = msgs;
  }



  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_DTS_MERGE_RESULT;
  }

  @Override
  public byte[] encode() {
    ChannelHandlerContext ctx = super.getChannelHandlerContext();
    ByteBuffer byteBuffer = ByteBuffer.allocate(msgs.length * 1024);
    byteBuffer.putShort((short) msgs.length);
    for (ResultMessage msg : msgs) {
      msg.setChannelHandlerContext(ctx);
      byte[] data = msg.encode();
      byteBuffer.putShort(msg.getTypeCode());
      byteBuffer.put(data);
    }

    byteBuffer.flip();
    int length = byteBuffer.limit();
    byte[] content = new byte[length + 4];
    intToBytes(length, content, 0);
    byteBuffer.get(content, 4, length);
    return content;
  }

  @Override
  public boolean decode(ByteBuf in) {
    int i = in.readableBytes();
    if (i < 4)
      return false;

    i -= 4;
    int length = in.readInt();
    if (i < length)
      return false;
    byte[] buffer = new byte[length];
    in.readBytes(buffer);
    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
    decode(byteBuffer);
    return true;
  }

  public void decode(ByteBuffer byteBuffer) {
    ChannelHandlerContext ctx = super.getChannelHandlerContext();
    short msgNum = byteBuffer.getShort();
    msgs = new ResultMessage[msgNum];
    for (int idx = 0; idx < msgNum; idx++) {
      short typeCode = byteBuffer.getShort();
      String className = typeMap.get(typeCode);
      MergedMessage message = null;
      try {
        message = (MergedMessage) Class.forName(className).newInstance();
      } catch (Exception e) {
        logger.error(className + " not found", e);
      }
      ((ResultMessage) message).setChannelHandlerContext(ctx);
      message.decode(byteBuffer);
      msgs[idx] = (ResultMessage) message;
    }
  }

}
