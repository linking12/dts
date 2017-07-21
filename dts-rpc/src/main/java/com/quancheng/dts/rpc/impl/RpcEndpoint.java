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

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.message.MergeMessage;
import com.taobao.txc.rpc.impl.RpcMessage;
import com.taobao.txc.rpc.util.AddressManager;
import com.taobao.txc.rpc.util.AddressManagerDiamondImpl;

import io.netty.channel.ChannelDuplexHandler;

/**
 * @author liushiming
 * @version RpcEndpoint.java, v 0.0.1 2017年7月21日 下午3:44:01 liushiming
 * @since JDK 1.8
 */
@SuppressWarnings("rawtypes")
public abstract class RpcEndpoint extends ChannelDuplexHandler {

  protected final ThreadPoolExecutor messageExecutor;
  protected ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(1);

  protected ConcurrentHashMap<Long, MessageFuture> futures =
      new ConcurrentHashMap<Long, MessageFuture>();
  private static final Logger logger = LoggerFactory.getLogger(RpcEndpoint.class);
  private int timeoutCheckInternal = 5000;
  protected AddressManager addressManager = new AddressManagerDiamondImpl();
  private String group = "DEFAULT";
  private Object lock = new Object();
  Map<String, BlockingQueue<RpcMessage>> basketMap =
      new ConcurrentHashMap<String, BlockingQueue<RpcMessage>>();
  protected Map<String, BlockingQueue<RpcMessage>> rmBasketMap =
      new ConcurrentHashMap<String, BlockingQueue<RpcMessage>>();
  protected Object mergeLock = new Object();
  protected Map<Long, MergeMessage> mergeMsgMap = new ConcurrentHashMap<Long, MergeMessage>();
  protected boolean isSending = false;

}
