package com.quancheng.dts.rpc.impl;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.message.DtsCodec;
import com.quancheng.dts.message.DtsMergeMessage;
import com.quancheng.dts.message.DtsMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

/**
 * 
 * @author liushiming
 * @version DtsMessageCodec.java, v 0.0.1 2017年8月10日 下午3:32:44 liushiming
 * @since JDK 1.8
 */
public class DtsMessageCodec extends ByteToMessageCodec<RpcMessage> {
  private static short MAGIC = (short) 0xdada;
  private static int HEAD_LENGHT = 14;
  // message flag.
  private static final int FLAG_REQUEST = 0x80; // 10000000
  private static final int FLAG_ASYNC = 0x40; // 01000000
  private static final int FLAG_HEARTBEAT = 0x20; // 00100000
  private static final int FLAG_TXCCODEC = 0x10; // 01000000
  private static final Logger logger = LoggerFactory.getLogger(DtsMessageCodec.class);

  @Override
  protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
    DtsCodec txcCodec = null;
    ByteBuffer byteBuffer = ByteBuffer.allocate(128);
    if (msg.getBody() instanceof DtsCodec) {
      txcCodec = (DtsCodec) msg.getBody();
    }
    byteBuffer.putShort(MAGIC);
    int flag = (msg.isAsync() ? FLAG_ASYNC : 0) | (msg.isHeartbeat() ? FLAG_HEARTBEAT : 0)
        | (msg.isRequest() ? FLAG_REQUEST : 0) | (txcCodec != null ? FLAG_TXCCODEC : 0);

    byteBuffer.putShort((short) flag);
    if (msg.getBody() instanceof HeartbeatMessage) {
      byteBuffer.putShort((short) 0);
      byteBuffer.putLong(msg.getId());
      byteBuffer.flip();
      byte[] content = new byte[byteBuffer.limit()];
      byteBuffer.get(content);
      out.writeBytes(content);
      return;
    }

    try {
      if (txcCodec != null) {
        txcCodec = (DtsCodec) msg.getBody();
        txcCodec.setChannelHandlerContext(ctx);
        byteBuffer.putShort(txcCodec.getTypeCode());
        byteBuffer.putLong(msg.getId());

        byteBuffer.flip();
        byte[] content = new byte[byteBuffer.limit()];
        byteBuffer.get(content);
        out.writeBytes(content);
        out.writeBytes(txcCodec.encode());
      } else {
        byte[] body = hessianSerialize(msg.getBody());
        byteBuffer.putShort((short) body.length);
        byteBuffer.putLong(msg.getId());
        byteBuffer.put(body);

        byteBuffer.flip();
        byte[] content = new byte[byteBuffer.limit()];
        byteBuffer.get(content);
        out.writeBytes(content);
      }
    } catch (Exception e) {
      logger.error("encode error", "", e);
      throw e;
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Send:" + msg.getBody());
    }
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("channeL:" + ctx.channel());
    }

    int readableBytes = in.readableBytes();
    if (readableBytes < HEAD_LENGHT) {
      return;
    }

    int begin = in.readerIndex();
    byte[] buffer = new byte[HEAD_LENGHT];
    in.readBytes(buffer);
    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

    short magic = byteBuffer.getShort();
    if (magic != MAGIC) {
      ctx.channel().close();
      return;
    }

    int flag = byteBuffer.getShort();
    boolean isHeartbeat = (FLAG_HEARTBEAT & flag) > 0;
    boolean isRequest = (FLAG_REQUEST & flag) > 0;
    boolean isTxcCodec = (FLAG_TXCCODEC & flag) > 0;

    short bodyLength = 0;
    short typeCode = 0;
    if (!isTxcCodec)
      bodyLength = byteBuffer.getShort();
    else
      typeCode = byteBuffer.getShort();
    long msgId = byteBuffer.getLong();

    if (isHeartbeat) {
      RpcMessage rpcMessage = new RpcMessage();
      rpcMessage.setId(msgId);
      rpcMessage.setAsync(true);
      rpcMessage.setHeartbeat(isHeartbeat);
      rpcMessage.setRequest(isRequest);

      if (isRequest) {
        rpcMessage.setBody(HeartbeatMessage.PING);
      } else {
        rpcMessage.setBody(HeartbeatMessage.PONG);
      }

      out.add(rpcMessage);
      return;
    }

    if (bodyLength > 0 && in.readableBytes() < bodyLength) {
      in.readerIndex(begin);
      return;
    }

    RpcMessage rpcMessage = new RpcMessage();
    rpcMessage.setId(msgId);
    rpcMessage.setAsync((FLAG_ASYNC & flag) > 0);
    rpcMessage.setHeartbeat(false);
    rpcMessage.setRequest(isRequest);

    try {
      if (isTxcCodec) {
        DtsCodec codec = getTxcCodecInstance(typeCode);
        codec.setChannelHandlerContext(ctx);
        if (codec.decode(in) == false) {
          in.readerIndex(begin);
          return;
        }
        rpcMessage.setBody(codec);
      } else {
        byte[] body = new byte[bodyLength];
        in.readBytes(body);
        Object bodyObject = hessianDeserialize(body);
        rpcMessage.setBody(bodyObject);
      }
    } catch (Exception e) {
      logger.error("decode error", "", e);
      throw e;
    }
    out.add(rpcMessage);
    if (logger.isDebugEnabled()) {
      logger.debug("Receive:" + rpcMessage.getBody() + ",messageId:" + msgId);
    }
  }

  /**
   * @param typeCode
   * @return
   */
  public DtsCodec getTxcCodecInstance(short typeCode) {
    DtsCodec codec;
    if (typeCode == DtsMessage.TYPE_DTS_MERGE)
      codec = new DtsMergeMessage();
    else {
      String className = DtsMessage.typeMap.get(typeCode);
      try {
        codec = (DtsCodec) Class.forName(className).newInstance();
      } catch (Exception e) {
        throw new DtsException(e);
      }
    }
    return codec;
  }

  /**
   * 
   * hessian序列化
   * 
   * @param object
   * @return
   * @throws Exception
   */
  public static byte[] hessianSerialize(Object object) throws Exception {
    if (object == null) {
      throw new NullPointerException();
    }

    throw new DtsException("hessianSerialize error");
  }


  public static Object hessianDeserialize(byte[] bytes) throws Exception {
    if (bytes == null) {
      throw new NullPointerException();
    }
    throw new DtsException("hessianDeserialize error");
  }
}
