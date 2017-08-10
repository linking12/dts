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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.common.DtsConstants;
import com.quancheng.dts.common.DtsContext;
import com.quancheng.dts.common.DtsErrCode;
import com.quancheng.dts.common.DtsException;
import com.quancheng.dts.common.DtsXID;
import com.quancheng.dts.common.ResultCode;
import com.quancheng.dts.message.DtsMergeMessage;
import com.quancheng.dts.message.DtsMergeResultMessage;
import com.quancheng.dts.message.DtsMessage;
import com.quancheng.dts.message.request.GlobalRollbackMessage;
import com.quancheng.dts.rpc.ClientMessageListener;
import com.quancheng.dts.rpc.DtsClientMessageSender;
import com.quancheng.dts.rpc.cluster.AddressWatcher;
import com.quancheng.dts.rpc.util.NetUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;


/**
 * @author liushiming
 * @version RpcClient.java, v 0.0.1 2017年7月25日 下午4:02:01 liushiming
 * @since JDK 1.8
 */
public class RpcClient extends RpcEndpoint implements DtsClientMessageSender {

  private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

  protected AtomicLong requestSeq = new AtomicLong(0);
  protected NioEventLoopGroup eventloopGroup = new NioEventLoopGroup(1);
  protected ClientMessageListener clientMessageListener;
  protected volatile List<String> serverAddressList = new ArrayList<String>();
  protected ConcurrentHashMap<String, Object> channelLocks =
      new ConcurrentHashMap<String, Object>();
  protected ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
  protected String clientAppName = System.getProperty("txc.appName", "txc_client");


  public RpcClient(ThreadPoolExecutor messageExecutor) {
    super(messageExecutor);
  }

  @Override
  public void init() {
    init(5, 5);
  }

  public void init(long healthCheckDelay, long healthCheckPeriod) {
    try {
      addressManager.getAddressList(this.getGroup(), new AddressWatcher() {
        @Override
        public void onAddressListChanged(List<String> newAddressList) {
          RpcClient.this.serverAddressList = newAddressList;
          logger.info("received new server list:" + newAddressList.toString());
          reconnect();
        }
      });
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    timerExecutor.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        reconnect();
      }
    }, healthCheckDelay, healthCheckPeriod, TimeUnit.SECONDS);

    new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          synchronized (mergeLock) {
            try {
              mergeLock.wait(1);
            } catch (InterruptedException e) {
            }
          }
          isSending = true;
          for (String address : basketMap.keySet()) {
            BlockingQueue<RpcMessage> basket = basketMap.get(address);
            if (basket.isEmpty())
              continue;

            DtsMergeMessage mergeMessage = new DtsMergeMessage();
            int idx = 0;
            while (!basket.isEmpty()) {
              RpcMessage msg = basket.poll();
              mergeMessage.msgs.add((DtsMergeMessage) msg.getBody());
              mergeMessage.msgIds.add(msg.getId());
              idx++;
            }

            if (idx > 1 && logger.isDebugEnabled()) {
              logger.debug("msgs:" + idx);
              for (DtsMessage cm : mergeMessage.msgs)
                logger.debug(cm.toString());
              StringBuffer sb = new StringBuffer();
              for (long l : mergeMessage.msgIds)
                sb.append("msgid:").append(l).append(";");
              sb.append("\n");
              for (long l : futures.keySet())
                sb.append("futures:").append(l).append(";");
              logger.debug(sb.toString());
            }

            try {
              sendRequest(connect(address), mergeMessage);
            } catch (Exception e) {
              logger.error("", "cluster merge call failed", e);
              e.printStackTrace();
            }
          }
          isSending = false;
        }
      }
    }).start();
    super.init();
  }

  private void reconnect() {
    for (String serverAddress : serverAddressList) {
      try {
        connect(serverAddress);
      } catch (Exception e) {
        logger.error(DtsErrCode.NetConnect.errCode,
            "can not connect to " + serverAddress + " cause:" + e.getMessage());
      }
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    eventloopGroup.shutdownGracefully();
  }

  @Override
  public void dispatch(long msgId, ChannelHandlerContext ctx, Object msg) {
    if (clientMessageListener != null) {
      clientMessageListener.onMessage(msgId, NetUtil.toStringAddress(ctx.channel().remoteAddress()),
          msg);
    }
  }

  @Override
  public Object invoke(Object msg, long timeout) throws IOException, TimeoutException {
    String validAddress = null;
    String svrAddr = DtsXID.getServerAddress(DtsContext.getCurrentXid());
    try {
      validAddress = getTargetServerAddress(svrAddr);
    } catch (Exception e) {
      logger.info("channel is not ok. " + e);
      if (msg instanceof GlobalRollbackMessage && DtsContext.getTxcNextSvrAddr() != null) {
        validAddress = getTargetServerAddress(DtsContext.getTxcNextSvrAddr());
        ((GlobalRollbackMessage) msg).setRealSvrAddr(svrAddr);
        logger.info("I will ask next node (" + DtsContext.getTxcNextSvrAddr()
            + ") to finish the rollback " + msg + ". real node is " + svrAddr);
      } else {
        if (e instanceof IOException)
          throw (IOException) e;
        else if (e instanceof TimeoutException)
          throw (TimeoutException) e;
        else
          throw new DtsException(e);
      }
    }
    return super.invoke(validAddress, connect(validAddress), msg, timeout);
  }

  protected String getTargetServerAddress(String serverAddress) {
    if (serverAddress != null) {
      return connect(serverAddress) == null ? null : serverAddress;
    } else {
      return balanceNextChannel().address;
    }
  }

  protected ChannelPackage balanceNextChannel() {
    int fetchCount = 0;
    while (true) {
      List<String> tmpServerAddressList = this.serverAddressList;
      if (tmpServerAddressList.size() == 0) {
        throw new DtsException(ResultCode.SYSTEMERROR.getValue(), "can not find txc server.");
      }
      int index = (int) (requestSeq.getAndIncrement() % tmpServerAddressList.size());
      String address = tmpServerAddressList.get(index);
      if (++fetchCount <= tmpServerAddressList.size()) {
        Channel channel = channels.get(address);
        if (channel != null && channel.isActive()) {
          return new ChannelPackage(channel, address);
        }
      } else {
        // 超出阈值,转入尝试连接模式
        return new ChannelPackage(connect(address), address);
      }
    }
  }

  @Override
  public Object invoke(Object msg) throws IOException, TimeoutException {
    return invoke(msg, DtsConstants.RPC_INVOKE_TIMEOUT);
  }

  @Override
  public Object invoke(String serverAddress, Object msg, long timeout)
      throws IOException, TimeoutException {
    return invoke(serverAddress, connect(serverAddress), msg, timeout);
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof RpcMessage) {
      RpcMessage rpcMessage = (RpcMessage) msg;
      if (rpcMessage.getBody() == HeartbeatMessage.PONG) {
        if (logger.isDebugEnabled()) {
          logger.debug("received PONG from " + ctx.channel().remoteAddress());
        }
        return;
      }
      if (((RpcMessage) msg).getBody() instanceof DtsMergeResultMessage) {
        DtsMergeResultMessage results = (DtsMergeResultMessage) ((RpcMessage) msg).getBody();
        DtsMergeMessage mergeMessage =
            (DtsMergeMessage) mergeMsgMap.remove(((RpcMessage) msg).getId());
        int num = mergeMessage.msgs.size();
        for (int i = 0; i < num; i++) {
          long msgId = mergeMessage.msgIds.get(i);
          MessageFuture future = futures.remove(msgId);
          if (future == null) {
            logger.info("msg:" + msgId + " is not found in futures.");
          } else {
            future.setResultMessage(results.getMsgs()[i]);
          }
        }
        return;
      }
    }
    super.channelRead(ctx, msg);
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
      if (idleStateEvent == IdleStateEvent.READER_IDLE_STATE_EVENT) {
        logger.info("close channle" + ctx.channel() + " after 10s read idle.");
        ctx.channel().close();
      }

      if (idleStateEvent == IdleStateEvent.WRITER_IDLE_STATE_EVENT) {
        RpcClient.this.sendRequest(ctx.channel(), HeartbeatMessage.PING);
      }
    }

  }

  protected Channel connect(String serverAddress) {
    Channel channelToServer = channels.get(serverAddress);
    if (channelToServer != null) {
      if (channelToServer.isActive())
        return channelToServer;
      else {
        int i = 0;
        for (i = 0; i < 1000; i++) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
          }
          channelToServer = channels.get(serverAddress);
          if (channelToServer == null)
            break;
          if (channelToServer.isActive())
            return channelToServer;
        }
        if (i == 1000) { // always be not active
          try {
            logger.warn("channel " + channelToServer + " is not active after long wait, close it.");
            Object connectLock = channelLocks.get(serverAddress);
            if (connectLock == null) {
              channelLocks.putIfAbsent(serverAddress, new Object());
              connectLock = channelLocks.get(serverAddress);
            }
            synchronized (connectLock) {
              Channel ch = channels.get(serverAddress);
              if (ch != null && ch.compareTo(channelToServer) == 0) {
                channels.remove(serverAddress);
                channelToServer.disconnect();
                channelToServer.close();
              }
            }
          } catch (Exception e) {
          } finally {
          }
        }
      }
    }

    Object connectLock = channelLocks.get(serverAddress);
    if (connectLock == null) {
      channelLocks.putIfAbsent(serverAddress, new Object());
      connectLock = channelLocks.get(serverAddress);
    }
    synchronized (connectLock) {
      channelToServer = _connect(serverAddress);
      channels.put(serverAddress, channelToServer);
      return channelToServer;
    }
  }

  protected Channel _connect(String serverAddress) {
    Channel channelToServer = channels.get(serverAddress);
    if (channelToServer != null && channelToServer.isActive()) {
      return channelToServer;
    }
    logger.info("connect to " + serverAddress);
    InetSocketAddress address = NetUtil.toInetSocketAddress(serverAddress);
    Bootstrap b = new Bootstrap();
    b.group(eventloopGroup).channel(NioSocketChannel.class).remoteAddress(address)
        .option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.SO_REUSEADDR, true).handler(new ChannelInitializer<SocketChannel>() {
          @Override
          public void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new IdleStateHandler(10, 5, 0), new DtsMessageCodec(),
                RpcClient.this);
          }
        });

    long start = System.currentTimeMillis();
    Object response = null;
    try {
      Channel tmpChannel = b.connect().sync().channel();
      try {
        response =
            super.invoke(null, tmpChannel, new RegisterClientAppNameMessage(clientAppName), 3000);
        if (response instanceof RegisterClientAppNameResultMessage) {
          if (((RegisterClientAppNameResultMessage) response).isResult()) {
            channelToServer = tmpChannel;
          } else {
            logger.info("register client app failed. server version:"
                + ((RegisterClientAppNameResultMessage) response).getVersion());
            throw new DtsException(ResultCode.SYSTEMERROR.getValue(),
                "register client app failed.");
          }
        } else {
          throw new DtsException(ResultCode.SYSTEMERROR.getValue(), "can not register app name.");
        }
      } catch (Exception e) {
        logger.error(DtsErrCode.NetRegAppname.errCode, "register client app failed.", e);
        throw new DtsException(ResultCode.SYSTEMERROR.getValue(), "can not register app.");
      }
    } catch (InterruptedException e) {
      throw new DtsException(e, "can not connect to txc server.");
    }

    logger.info("register client app sucesss. server cost " + (System.currentTimeMillis() - start)
        + " ms, version:" + ((RegisterClientAppNameResultMessage) response).getVersion());
    return channelToServer;
  }

  public ClientMessageListener getClientMessageListener() {
    return clientMessageListener;
  }

  public void setClientMessageListener(ClientMessageListener clientMessageListener) {
    this.clientMessageListener = clientMessageListener;
  }

  public String getClientAppName() {
    return clientAppName;
  }

  public void setClientAppName(String clientAppName) {
    this.clientAppName = clientAppName;
  }

  @Override
  public void sendResponse(long msgId, String serverAddress, Object msg) {
    super.sendResponse(msgId, connect(serverAddress), msg);
  }

  public final static class ChannelPackage {
    public final Channel channel;
    public final String address;

    public ChannelPackage(Channel channel, String address) {
      super();
      this.channel = channel;
      this.address = address;
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.error(DtsErrCode.ExceptionCaught.errCode,
        NetUtil.toStringAddress(ctx.channel().remoteAddress()) + "connect exception. "
            + cause.getMessage(),
        cause);
    Iterator<Entry<String, Channel>> it = channels.entrySet().iterator();
    while (it.hasNext()) {
      if (it.next().getValue().compareTo(ctx.channel()) == 0) {
        it.remove();
        logger.info("remove channel:" + ctx.channel());
      }
    }
  }

}
