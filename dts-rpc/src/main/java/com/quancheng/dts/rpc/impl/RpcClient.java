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
package com.quancheng.dts.rpc.impl;

import java.util.concurrent.ThreadPoolExecutor;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author liushiming
 * @version RpcClient.java, v 0.0.1 2017年7月25日 下午4:02:01 liushiming
 * @since JDK 1.8
 */
public class RpcClient extends RpcEndpoint {

  public RpcClient(ThreadPoolExecutor messageExecutor) {
    super(messageExecutor);
  }

  /**
   * @see com.quancheng.dts.rpc.impl.RpcEndpoint#dispatch(long,
   *      io.netty.channel.ChannelHandlerContext, java.lang.Object)
   */
  @Override
  public void dispatch(long msgId, ChannelHandlerContext ctx, Object msg) {

  }

}
