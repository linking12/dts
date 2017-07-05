package com.quancheng.dts.message;

import java.nio.ByteBuffer;

/**
 * 合并的消息
 * 
 * @author liushiming
 * @version MergedMessage.java, v 0.0.1 2017年7月5日 下午5:29:53 liushiming
 * @since JDK 1.8
 */
public interface MergedMessage {

  /**
   * 解码
   * 
   * @author liushiming
   * @param byteBuffer
   * @since JDK 1.8
   */
  public void decode(ByteBuffer byteBuffer);
}
