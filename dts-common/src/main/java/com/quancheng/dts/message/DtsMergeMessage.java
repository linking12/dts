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
import java.util.ArrayList;
import java.util.List;

import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.message.request.BeginMessage;
import com.quancheng.dts.message.request.GlobalCommitMessage;
import com.quancheng.dts.message.request.GlobalRollbackMessage;
import com.quancheng.dts.message.request.QueryLockMessage;
import com.quancheng.dts.message.request.RegisterMessage;
import com.quancheng.dts.message.request.ReportStatusMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author liushiming
 * @version DtsMergeMessage.java, v 0.0.1 2017年7月5日 下午5:40:49 liushiming
 * @since JDK 1.8
 */
public class DtsMergeMessage extends DtsMessage implements MergeMessage {

  private static final long serialVersionUID = 4190062406263786100L;

  public List<DtsMessage> msgs = new ArrayList<DtsMessage>();

  public List<Long> msgIds = new ArrayList<Long>();

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_DTS_MERGE;
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#encode()
   */
  @Override
  public byte[] encode() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(msgs.size() * 1024);
    byteBuffer.putShort((short) msgs.size());
    ChannelHandlerContext ctx = super.getChannelHandlerContext();
    for (DtsMessage msg : msgs) {
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

  private void decode(ByteBuffer byteBuffer) {
    short msgNum = byteBuffer.getShort();
    ChannelHandlerContext ctx = super.getChannelHandlerContext();
    for (int idx = 0; idx < msgNum; idx++) {
      short typeCode = byteBuffer.getShort();
      MergedMessage message = null;
      switch (typeCode) {
        case TYPE_BEGIN:
          message = new BeginMessage();
          break;
        case TYPE_REGIST:
          message = new RegisterMessage();
          break;
        case TYPE_REPORT_STATUS:
          message = new ReportStatusMessage();
          break;
        case TYPE_GLOBAL_COMMIT:
          message = new GlobalCommitMessage();
          break;
        case TYPE_GLOBAL_ROLLBACK:
          message = new GlobalRollbackMessage();
          break;
        case TYPE_QUERY_LOCK:
          message = new QueryLockMessage();
          break;
        default:
          String className = typeMap.get(typeCode);
          try {
            message = (MergedMessage) Class.forName(className).newInstance();
          } catch (Exception e) {
            throw new DtsException(e);
          }
          break;
      }
      ((DtsMessage) message).setChannelHandlerContext(ctx);
      message.decode(byteBuffer);
      msgs.add((DtsMessage) message);
    }
  }


}
