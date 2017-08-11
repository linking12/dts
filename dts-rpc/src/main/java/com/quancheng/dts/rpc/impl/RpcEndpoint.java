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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.common.DtsException;
import com.quancheng.dts.common.ResultCode;
import com.quancheng.dts.message.MergeMessage;
import com.quancheng.dts.message.request.ClusterDumpMessage;
import com.quancheng.dts.rpc.cluster.AddressManager;
import com.quancheng.dts.rpc.cluster.ZookeeperAddressManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author liushiming
 * @version RpcEndpoint.java, v 0.0.1 2017年7月21日 下午3:44:01 liushiming
 * @since JDK 1.8
 */
public abstract class RpcEndpoint extends ChannelDuplexHandler {

  private static final Logger logger = LoggerFactory.getLogger(RpcEndpoint.class);
  protected final ThreadPoolExecutor messageExecutor;
  protected ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(1);
  protected ConcurrentHashMap<Long, MessageFuture> futures =
      new ConcurrentHashMap<Long, MessageFuture>();
  private int timeoutCheckInternal = 5000;
  protected AddressManager addressManager = new ZookeeperAddressManager();
  private String group = "DEFAULT";
  private Object lock = new Object();
  Map<String, BlockingQueue<RpcMessage>> basketMap =
      new ConcurrentHashMap<String, BlockingQueue<RpcMessage>>();
  protected Map<String, BlockingQueue<RpcMessage>> rmBasketMap =
      new ConcurrentHashMap<String, BlockingQueue<RpcMessage>>();
  protected Object mergeLock = new Object();
  protected Map<Long, MergeMessage> mergeMsgMap = new ConcurrentHashMap<Long, MergeMessage>();
  protected boolean isSending = false;

  public RpcEndpoint(ThreadPoolExecutor messageExecutor) {
    this.messageExecutor = messageExecutor;
  }

  public void init() {
    timerExecutor.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        List<MessageFuture> timeoutMessageFutures = new ArrayList<MessageFuture>(futures.size());
        for (MessageFuture future : futures.values()) {
          if (future.isTimeout()) {
            timeoutMessageFutures.add(future);
          }
        }
        for (MessageFuture messageFuture : timeoutMessageFutures) {
          futures.remove(messageFuture.getRequestMessage().getId());
          messageFuture.setResultMessage(null);
          if (logger.isDebugEnabled()) {
            logger.debug("timeout clear future : " + messageFuture);
          }
        }
      }
    }, timeoutCheckInternal, timeoutCheckInternal, TimeUnit.MILLISECONDS);
  }

  public void destroy() {
    timerExecutor.shutdown();
  }

  public AddressManager getAddressManager() {
    return addressManager;
  }

  public void setAddressManager(AddressManager addressManager) {
    this.addressManager = addressManager;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  protected Object invoke(String address, Channel channel, Object msg, long timeout)
      throws IOException, TimeoutException {
    if (channel == null) {
      logger.warn("invoke nothing, caused by null channel.");
      return null;
    }
    boolean waitResponse = timeout >= 0 ? true : false;
    final RpcMessage rpcMessage = new RpcMessage();
    rpcMessage.setId(RpcMessage.getNextMessageId());
    rpcMessage.setAsync(false);
    rpcMessage.setHeartbeat(false);
    rpcMessage.setRequest(true);
    rpcMessage.setBody(msg);
    final MessageFuture messageFuture = new MessageFuture();
    messageFuture.setRequestMessage(rpcMessage);
    messageFuture.setTimeout(waitResponse ? timeout : 30 * 1000);
    futures.put(rpcMessage.getId(), messageFuture);
    if (address != null && !(msg instanceof ClusterDumpMessage)) {
      Map<String, BlockingQueue<RpcMessage>> map = null;
      if (this instanceof RpcClient) {
        map = basketMap;
      } else {
        map = rmBasketMap;
      }
      BlockingQueue<RpcMessage> basket = map.get(address);
      if (basket == null) {
        basket = new LinkedBlockingQueue<RpcMessage>();
        map.put(address, basket);
      }
      basket.offer(rpcMessage);
      if (!isSending) {
        synchronized (mergeLock) {
          mergeLock.notify();
        }
      }
    } else {
      ChannelFuture future;
      if (logger.isDebugEnabled()) {
        logger.debug(String.format("%s wanted to send msgid:%s body:%s future:%s", this,
            rpcMessage.getId(), rpcMessage.getBody(), messageFuture));
      }

      synchronized (lock) {
        int tryTimes = 0;
        while (!channel.isWritable()) {
          try {
            tryTimes++;
            if (tryTimes > 3000)
              throw new DtsException("channel is not writable");
            lock.wait(10);
          } catch (InterruptedException e) {
          }
        }
      }
      future = channel.writeAndFlush(rpcMessage);
      future.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          if (!future.isSuccess()) {
            MessageFuture messageFuture = futures.remove(rpcMessage.getId());
            if (messageFuture != null)
              messageFuture.setResultMessage(future.cause());
            future.channel().close();
          }
        }
      });
    }
    if (waitResponse) {
      try {
        return messageFuture.get(timeout, TimeUnit.MILLISECONDS);
      } catch (Exception e) {
        if (logger.isDebugEnabled()) {
          logger.debug("messageFuture : " + messageFuture);
        }
        throw new RuntimeException(e);
      }
    } else {
      return null;
    }
  }


  @Override
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    synchronized (lock) {
      if (ctx.channel().isWritable()) {
        lock.notify();
      }
    }
    ctx.fireChannelWritabilityChanged();
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof RpcMessage) {
      final RpcMessage rpcMessage = (RpcMessage) msg;
      if (rpcMessage.isRequest()) {
        this.doProccessRequestMessage(ctx, rpcMessage);
      } else {
        this.doProcessResponseMessage(ctx, rpcMessage);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.error(cause.getMessage(), cause);
  }


  private void doProcessResponseMessage(final ChannelHandlerContext ctx,
      final RpcMessage rpcMessage) {
    MessageFuture messageFuture = futures.remove(rpcMessage.getId());
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s msgId:%s, future :%s, body:%s", this, rpcMessage.getId(),
          messageFuture, rpcMessage.getBody()));
    }
    if (messageFuture != null) {
      messageFuture.setResultMessage(rpcMessage);
    } else {
      try {
        RpcEndpoint.this.messageExecutor.execute(new Runnable() {
          @Override
          public void run() {
            try {
              RpcEndpoint.this.dispatch(rpcMessage.getId(), ctx, rpcMessage.getBody());
            } catch (Throwable th) {
              logger.error(th.getMessage(), th);
              sendErrorResponse(ctx.channel(), rpcMessage.getId(), th);
            }
          }
        });
      } catch (RejectedExecutionException e) {
        logger.error(
            "thread pool is full, current max pool size is " + messageExecutor.getActiveCount());
        sendErrorResponse(ctx.channel(), rpcMessage.getId(), e);
      }
    }
  }

  private void doProccessRequestMessage(final ChannelHandlerContext ctx,
      final RpcMessage rpcMessage) {
    if (logger.isDebugEnabled()) {
      logger.debug(
          String.format("%s msgId:%s, body:%s", this, rpcMessage.getId(), rpcMessage.getBody()));
    }
    try {
      RpcEndpoint.this.messageExecutor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            RpcEndpoint.this.dispatch(rpcMessage.getId(), ctx, rpcMessage.getBody());
          } catch (Throwable th) {
            logger.error(th.getMessage(), th);
            try {
              sendErrorResponse(ctx.channel(), rpcMessage.getId(), th);
            } catch (Exception e) {
              logger.error("send error response fail.", e);
            }
          }
        }
      });
    } catch (RejectedExecutionException e) {
      logger.error(
          "thread pool is full, current max pool size is " + messageExecutor.getActiveCount());
      sendErrorResponse(ctx.channel(), rpcMessage.getId(), e);
    }
  }

  protected void sendErrorResponse(Channel channel, long msgId, Throwable th) {
    DtsException txcException =
        new DtsException(ResultCode.SYSTEMERROR.getValue(), th.getMessage());
    txcException.setStackTrace(th.getStackTrace());
    RpcMessage rpcMessage = new RpcMessage();
    rpcMessage.setAsync(true);
    rpcMessage.setHeartbeat(false);
    rpcMessage.setRequest(false);
    rpcMessage.setBody(txcException);
    rpcMessage.setId(msgId);
    if (!channel.isWritable()) {
      throw new DtsException("channel is not writable");
    }
    channel.writeAndFlush(rpcMessage);
  }

  protected void sendRequest(Channel channel, Object msg) {
    RpcMessage rpcMessage = new RpcMessage();
    rpcMessage.setAsync(true);
    rpcMessage.setHeartbeat(msg instanceof HeartbeatMessage);
    rpcMessage.setRequest(true);
    rpcMessage.setBody(msg);
    rpcMessage.setId(RpcMessage.getNextMessageId());
    if (msg instanceof MergeMessage)
      mergeMsgMap.put(rpcMessage.getId(), (MergeMessage) msg);

    synchronized (lock) {
      int tryTimes = 0;
      while (!channel.isWritable()) {
        try {
          tryTimes++;
          if (tryTimes > 3000)
            throw new DtsException("channel is not writable");
          lock.wait(10);
        } catch (InterruptedException e) {
        }
      }
    }
    channel.writeAndFlush(rpcMessage);
  }

  protected void sendResponse(long msgId, Channel channel, Object msg) {
    RpcMessage rpcMessage = new RpcMessage();
    rpcMessage.setAsync(true);
    rpcMessage.setHeartbeat(msg instanceof HeartbeatMessage);
    rpcMessage.setRequest(false);
    rpcMessage.setBody(msg);
    rpcMessage.setId(msgId);
    synchronized (lock) {
      int tryTimes = 0;
      while (!channel.isWritable()) {
        try {
          tryTimes++;
          if (tryTimes > 3000)
            throw new DtsException("channel is not writable");
          lock.wait(10);
        } catch (InterruptedException e) {
        }
      }
    }
    if (logger.isDebugEnabled())
      logger.debug("send response:" + rpcMessage.getBody() + ",channel:" + channel);
    channel.writeAndFlush(rpcMessage);
  }

  public abstract void dispatch(long msgId, ChannelHandlerContext ctx, Object msg);
}
