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

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.quancheng.dts.message.response.ResultMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author liushiming
 * @version DtsMessage.java, v 0.0.1 2017年7月5日 下午5:07:15 liushiming
 * @since JDK 1.8
 */
public abstract class DtsMessage implements DtsMsgVistor, DtsCodec, Serializable {

  private static final long serialVersionUID = -1591010533701042512L;
  public static final Charset UTF8 = Charset.forName("utf-8");


  public static final short TYPE_BEGIN = 1;
  public static final short TYPE_BEGIN_RESULT = 2;

  public static final short TYPE_BRANCH_COMMIT = 3;
  public static final short TYPE_BRANCH_COMMIT_RESULT = 4;

  public static final short TYPE_BRANCH_ROLLBACK = 5;
  public static final short TYPE_BRANCH_ROLLBACK_RESULT = 6;

  public static final short TYPE_GLOBAL_COMMIT = 7;
  public static final short TYPE_GLOBAL_COMMIT_RESULT = 8;

  public static final short TYPE_GLOBAL_ROLLBACK = 9;
  public static final short TYPE_GLOBAL_ROLLBACK_RESULT = 10;

  public static final short TYPE_REGIST = 11;
  public static final short TYPE_REGIST_RESULT = 12;

  public static final short TYPE_REPORT_STATUS = 13;
  public static final short TYPE_REPORT_STATUS_RESULT = 14;

  public static final short TYPE_BEGIN_RETRY_BRANCH = 15;
  public static final short TYPE_BEGIN_RETRY_BRANCH_RESULT = 16;

  public static final short TYPE_REPORT_UDATA = 17;
  public static final short TYPE_REPORT_UDATA_RESULT = 18;

  public static final short TYPE_DTS_MERGE = 19;
  public static final short TYPE_DTS_MERGE_RESULT = 20;

  public static final short TYPE_QUERY_LOCK = 21;
  public static final short TYPE_QUERY_LOCK_RESULT = 22;



  public static final Map<Short, String> typeMap = new HashMap<Short, String>();

  static {


  }

  private ChannelHandlerContext ctx;

  private DtsMsgHandler handler;

  @Override
  public boolean decode(ByteBuf in) {
    return false;
  }

  @Override
  public void setChannelHandlerContext(ChannelHandlerContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ChannelHandlerContext getChannelHandlerContext() {
    return this.ctx;
  }

  public DtsMsgHandler getHandler() {
    return handler;
  }

  public void setHandler(DtsMsgHandler handler) {
    this.handler = handler;
  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      DtsMessage message, ResultMessage[] results, int idx) {
    throw new UnsupportedOperationException(this.getClass().getName());
  }



}
