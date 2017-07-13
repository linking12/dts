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
 * @version ReportUdataResultMessage.java, v 0.0.1 2017年7月13日 上午10:39:17 liushiming
 * @since JDK 1.8
 */
public class ReportUdataResultMessage extends ResultMessage {

  private static final long serialVersionUID = -649423000599706696L;

  public ReportUdataResultMessage() {
    super(256);
  }

  /**
   * @see com.quancheng.dts.message.DtsCodec#getTypeCode()
   */
  @Override
  public short getTypeCode() {
    return TYPE_REPORT_UDATA_RESULT;
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

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ReportUdataResultMessage [toString()=" + super.toString() + "]";
  }

}
