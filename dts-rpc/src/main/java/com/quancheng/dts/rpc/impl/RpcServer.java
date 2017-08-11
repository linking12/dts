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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.common.DtsConstants;
import com.quancheng.dts.common.DtsErrCode;
import com.quancheng.dts.common.DtsException;
import com.quancheng.dts.common.DtsXID;
import com.quancheng.dts.message.request.RegisterMessage;
import com.quancheng.dts.rpc.ConnectionMessage;
import com.quancheng.dts.rpc.DtsServerMessageSender;
import com.quancheng.dts.rpc.ServerMessageListener;
import com.quancheng.dts.rpc.util.NetUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author liushiming
 * @version RpcServer.java, v 0.0.1 2017年8月11日 下午2:44:04 liushiming
 * @since JDK 1.8
 */
public abstract class RpcServer extends RpcEndpoint implements DtsServerMessageSender {

  /**
   * @param messageExecutor
   */
  public RpcServer(ThreadPoolExecutor messageExecutor) {
    super(messageExecutor);
  }

  private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

  protected int port;
  protected ServerMessageListener serverMessageListener;
  protected ConcurrentHashMap<String, String> rmIpAndPortToClientAppName =
      new ConcurrentHashMap<String, String>();

  public static int mid;

  public ServerMessageListener getServerMessageListener() {
    return serverMessageListener;
  }

  public void setServerMessageListener(ServerMessageListener serverMessageListener) {
    this.serverMessageListener = serverMessageListener;
  }

  private NioEventLoopGroup bossGroup = new NioEventLoopGroup();
  private NioEventLoopGroup workerGroup = new NioEventLoopGroup();

  protected ConcurrentHashMap<String, String> ipAndPortToClientAppName =
      new ConcurrentHashMap<String, String>();
  protected ConcurrentHashMap<String, Channel> ipAndClientAppNameToChannelMap =
      new ConcurrentHashMap<String, Channel>();

  protected ConcurrentHashMap<String, String> ipAndPortToDbKey =
      new ConcurrentHashMap<String, String>();
  protected ConcurrentHashMap<String, Map<String, Map<Integer, Channel>>> dbKeyToChannelMap =
      new ConcurrentHashMap<String, Map<String, Map<Integer, Channel>>>();
  protected ConcurrentHashMap<String, Map<String, Map<Integer, Channel>>> dbKeyToRtChannelMap =
      new ConcurrentHashMap<String, Map<String, Map<Integer, Channel>>>();

  /**
   * 一个集群环的节点数，缺省为3；1备份在2；2备份在3；3备份在1
   */
  public static final int LOOP_CAPACITY = 3;

  /**
   * 备份节点与原节点的MID差值，缺省为100；比如mid为1的节点，它的备份标识为101；
   */
  public static final int BKUP_MID_DIFF = 100;

  /**
   * 得到自己替对方节点保存事务日志的MID；比如自己的MID为3，则自己替MID为2的节点备份事务日志，备份标识为102；
   * 对方节点从库里找到MID为(2+BKUP_MID_DIFF=102)的日志，可以进行恢复
   * 
   * @return
   */
  public static int getPeerBkupMid() {
    int i = mid - 1;
    if (i == 0)
      i = LOOP_CAPACITY;
    return i + BKUP_MID_DIFF;
  }

  /**
   * 得到自己替对方节点保存事务日志的MID；
   * 
   * @return
   */
  public static int getPeerMid() {
    int i = mid - 1;
    if (i == 0)
      i = LOOP_CAPACITY;
    return i;
  }

  /**
   * 得到自己的备份MID
   * 
   * @return
   */
  public static int getBkupMid() {
    return mid + BKUP_MID_DIFF;
  }

  @Override
  public void init() {
    super.init();
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
        .childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          public void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new IdleStateHandler(10, 0, 0)).addLast(new DtsMessageCodec(),
                RpcServer.this);
          }
        });

    DtsXID.setIpAddress(NetUtil.getLocalIp());
    DtsXID.setPort(port);
    String serverAddress = NetUtil.getLocalIp() + ":" + port;
    try {
      bootstrap.bind(NetUtil.toInetSocketAddress(serverAddress)).sync();
      logger.info("txc server begin to listen address:" + serverAddress);
    } catch (InterruptedException e) {
      throw new RuntimeException("txc server can not bind to local address:" + serverAddress);
    }
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
      if (idleStateEvent == IdleStateEvent.READER_IDLE_STATE_EVENT) {
        logger.info("close channel" + ctx.channel() + " after 10s read idle.");
        ctx.channel().close();
      }
    }
  }

  @Override
  public void destroy() {
    addressManager.unpublish(this.getGroup(), NetUtil.getLocalIp() + ":" + port);
    super.destroy();
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
  }

  public Channel getChannel(Map<String, Map<Integer, Channel>> chMap, String clientIp,
      String clientAppName) {
    Channel channel = null;
    Integer key = 0;
    String targetIp = "";
    if (chMap != null) {
      if ((chMap.get(clientIp) != null) && chMap.get(clientIp).size() > 0) {
        if (chMap.get(clientIp).size() > 1 && clientAppName != null) {
          for (Integer port : chMap.get(clientIp).keySet()) {
            if (rmIpAndPortToClientAppName.get(clientIp + ":" + port)
                .compareTo(clientAppName) == 0) {
              channel = chMap.get(clientIp).get(port);
              key = port;
              targetIp = clientIp;
              break;
            }
          }
        } else {
          key = chMap.get(clientIp).keySet().iterator().next();
          channel = chMap.get(clientIp).get(key);
          targetIp = clientIp;
        }
      }

      /**
       * 如果没有匹配的资源客户端IP，可以用另一个代替
       */
      if (channel == null && chMap.size() > 0) {
        for (String ip : chMap.keySet()) {
          Map<Integer, Channel> m = chMap.get(ip);
          if (m.size() > 1 && clientAppName != null) {
            for (Integer port : m.keySet()) {
              if (rmIpAndPortToClientAppName.get(ip + ":" + port).compareTo(clientAppName) == 0) {
                channel = m.get(port);
                targetIp = ip;
                key = port;
                break;
              }
            }
          } else if (m.size() == 1) {
            key = m.keySet().iterator().next();
            channel = m.get(key);
            targetIp = ip;
          }
          if (channel != null)
            break;
        }
      }

      if (channel != null) {
        if (channel.isActive()) {
          return channel;
        } else {
          for (int i = 0; i < 1000; i++) {
            try {
              Thread.sleep(1);
            } catch (InterruptedException e) {
            }
            if (channel.isActive()) {
              return channel;
            }
          }

          // channel is not active after long wait, close it...
          try {
            logger.warn("channel " + channel + " is not active after long wait, close it.");
            channel.disconnect();
            channel.close();
          } catch (Exception e) {
          } finally {
            chMap.get(targetIp).remove(key);
            if (chMap.get(targetIp).size() == 0)
              chMap.remove(targetIp);
            channel = null;
          }
        }
      }
    }
    return channel;
  }

  @Override
  public void sendRtRequest(String dbKey, String clientIp, String clientAppName, Object msg) {
    Channel clientChannel = null;
    if (dbKey != null) {
      Map<String, Map<Integer, Channel>> chMap = dbKeyToRtChannelMap.get(dbKey);
      clientChannel = this.getChannel(chMap, clientIp, clientAppName);
    }

    if (clientChannel != null) {
      super.sendRequest(clientChannel, msg);
    } else {
      throw new RuntimeException("client is not connected.");
    }
  }

  private Channel getChannel(String clientIp, String clientAppName) {
    Channel clientChannel = ipAndClientAppNameToChannelMap.get(clientIp + clientAppName);
    if (clientChannel == null)
      throw new DtsException("client is not connected.");
    if (clientChannel.isActive()) {
      return clientChannel;
    } else {
      for (int i = 0; i < 1000; i++) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        if (clientChannel.isActive()) {
          return clientChannel;
        }
      }

      // channel is not active after long wait, close it...
      try {
        logger.warn("channel " + clientChannel + " is not active after long wait, close it.");
        clientChannel.disconnect();
        clientChannel.close();
      } catch (Exception e) {

      } finally {
        ipAndClientAppNameToChannelMap.remove(clientIp + clientAppName);
        clientChannel = null;
      }
    }
    return clientChannel;
  }

  @Override
  public void sendRequest(String dbKey, String clientIp, String clientAppName, Object msg) {
    Channel clientChannel = null;
    Map<String, Map<Integer, Channel>> chMap = null;
    if (dbKey != null) {
      chMap = dbKeyToChannelMap.get(dbKey);
      clientChannel = getChannel(chMap, clientIp, clientAppName);
    } else if (clientIp != null) {
      clientChannel = getChannel(clientIp, clientAppName);
    } else {
      throw new RuntimeException("client is not connected.");
    }
    if (clientChannel == null)
      throw new DtsException("rm client is not connected. dbkey:" + dbKey + ",clientIp:" + clientIp
          + ",chMap:" + chMap);
    else
      super.sendRequest(clientChannel, msg);
  }

  @Override
  public void sendResponse(long msgId, String dbKey, String clientIp, String clientAppName,
      Object msg) {
    Channel clientChannel = null;
    if (dbKey != null) {
      Map<String, Map<Integer, Channel>> chMap = dbKeyToChannelMap.get(dbKey);
      clientChannel = getChannel(chMap, clientIp, clientAppName);
      if (clientChannel != null) {
        super.sendResponse(msgId, clientChannel, msg);
      } else {
        logger.info("chMap:" + chMap + ", msgId:" + msgId + ",dbKey:" + dbKey + ",clientIp"
            + clientIp + ",clientAppName:" + clientAppName + ",msg:" + msg);
        logger.info("rpcserver:" + this + ", dbKeyToChannelMap:" + dbKeyToChannelMap);
        throw new RuntimeException("RM is not connected.");
      }
    } else if (clientAppName != null && clientIp != null) {
      clientChannel = getChannel(clientIp, clientAppName);
      if (clientChannel != null) {
        super.sendResponse(msgId, clientChannel, msg);
      } else {
        throw new RuntimeException("client is not connected. dbkey:" + dbKey + ",clientIp:"
            + clientIp + ",clientAppName:" + clientAppName);
      }
    } else {
      throw new RuntimeException(
          "dbKey is null, clientAppName is" + clientAppName + ",clientIp is" + clientIp);
    }
  }

  @Override
  public void sendResponse(long msgId, Channel channel, Object msg) {
    if (channel != null && channel.isActive()) {
      super.sendResponse(msgId, channel, msg);
    } else {
      throw new RuntimeException("channel is not active. channel:" + channel);
    }
  }

  @Override
  public Object invoke(String dbKey, String clientIp, String clientAppName, Object msg,
      long timeout) throws IOException, TimeoutException {
    Channel clientChannel = null;
    Map<String, Map<Integer, Channel>> chMap = null;
    if (dbKey != null) {
      chMap = dbKeyToChannelMap.get(dbKey);
      clientChannel = getChannel(chMap, clientIp, clientAppName);
    } else if (clientAppName != null)
      clientChannel = getChannel(clientIp, clientAppName);

    if (clientChannel != null) {
      return super.invoke(null, clientChannel, msg, timeout);
    } else {
      throw new RuntimeException(
          "client is not connected. dbkey:" + dbKey + ",clientIp:" + clientIp + ",chMap:" + chMap);
    }
  }

  @Override
  public Object invoke(String dbKey, String clientIp, String clientAppName, Object msg)
      throws IOException, TimeoutException {
    return invoke(dbKey, clientIp, clientAppName, msg, DtsConstants.RPC_INVOKE_TIMEOUT);
  }

  @Override
  public void dispatch(long msgId, ChannelHandlerContext ctx, Object msg) {
    if (DtsConstants.isHandedChannel(ctx.channel())) {
      String ipAndPort = NetUtil.toStringAddress(ctx.channel().remoteAddress());
      String clientAppName = ipAndPortToClientAppName.get(ipAndPort);
      String dbKeys = ipAndPortToDbKey.get(ipAndPort);

      if (clientAppName != null || dbKeys != null) {
        serverMessageListener.onMessage(msgId, dbKeys,
            ipAndPort.substring(0, ipAndPort.indexOf(':')), clientAppName, msg);
      } else {
        if (msg instanceof RegisterMessage) {
          logger.warn("shouldn't com here," + ipAndPort);
        }
        serverMessageListener.onMessage(msgId, ctx.channel(), msg);
      }
    } else {
      ctx.close();
      logger.warn(String.format("close a unhanded connection! [%s]", ctx.channel().toString()));
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (messageExecutor.isShutdown()) {
      return;
    }
    final String ipAndPort = NetUtil.toStringAddress(ctx.channel().remoteAddress());
    logger.info(ipAndPort + " disconnect to server.");

    final String clientAppName = ipAndPortToClientAppName.remove(ipAndPort);
    final String dbKeys = ipAndPortToDbKey.remove(ipAndPort);

    if (clientAppName != null) {
      final String ip = ipAndPort.substring(0, ipAndPort.indexOf(':'));
      ipAndPortToClientAppName.remove(ip + clientAppName);
      messageExecutor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            ConnectionMessage message = new ConnectionMessage();
            message.setClientAppName(clientAppName);
            message.setClientIp(ip);
            message.setClientIpAndPort(ipAndPort);
            message.setConnected(false);
            serverMessageListener.onMessage(0, ip, clientAppName, message);
          } catch (Throwable th) {
            logger.error(DtsErrCode.NetOnMessage.errCode, th.getMessage(), th);
          }
        }
      });
    } else if (dbKeys != null) {
      final String ip = ipAndPort.substring(0, ipAndPort.indexOf(':'));
      int port = Integer.parseInt(ipAndPort.substring(ipAndPort.indexOf(':') + 1));
      String[] dbKeyArray = dbKeys.split(",");
      for (String dbKey : dbKeyArray) {
        Map<String, Map<Integer, Channel>> channelMap = dbKeyToChannelMap.get(dbKey);
        if (channelMap != null && channelMap.get(ip) != null) {
          channelMap.get(ip).remove(port);
        }

        channelMap = dbKeyToRtChannelMap.get(dbKey);
        if (channelMap != null && channelMap.get(ip) != null) {
          channelMap.get(ip).remove(port);
        }
      }
      messageExecutor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            ConnectionMessage message = new ConnectionMessage();
            message.setDbKeys(dbKeys);
            message.setClientIp(ip);
            message.setClientIpAndPort(ipAndPort);
            message.setConnected(false);
            serverMessageListener.onMessage(0, ip, clientAppName, message);
          } catch (Throwable th) {
            logger.error("0105", th.getMessage(), th);
          }
        }
      });
    } else { // unknown connection, might be cluster connection
      final String ip = ipAndPort.substring(0, ipAndPort.indexOf(':'));
      messageExecutor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            ConnectionMessage message = new ConnectionMessage();
            message.setClientIp(ip);
            message.setClientIpAndPort(ipAndPort);
            message.setConnected(false);
            serverMessageListener.onMessage(0, ip, clientAppName, message);
          } catch (Throwable th) {
            logger.error(DtsErrCode.NetOnMessage.errCode, th.getMessage(), th);
          }
        }
      });
    }
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof RpcMessage) {
      RpcMessage rpcMessage = (RpcMessage) msg;
      if (rpcMessage.getBody() instanceof RegisterClientAppNameMessage) {
        String ipAndPort = NetUtil.toStringAddress(ctx.channel().remoteAddress());
        RegisterClientAppNameMessage registerClientAppNameMessage =
            (RegisterClientAppNameMessage) rpcMessage.getBody();
        DtsConstants.addChannelVersion(ctx.channel(), registerClientAppNameMessage.getVersion());

        ipAndPortToClientAppName.put(ipAndPort, registerClientAppNameMessage.getClientAppName());

        String ipAndClientAppName = ipAndPort.substring(0, ipAndPort.indexOf(':'))
            + registerClientAppNameMessage.getClientAppName();
        ipAndClientAppNameToChannelMap.put(ipAndClientAppName, ctx.channel());

        sendResponse(rpcMessage.getId(), ctx.channel(),
            new RegisterClientAppNameResultMessage(true));

        logger.info("app " + registerClientAppNameMessage.getClientAppName() + " is connected from "
            + ipAndPort + ", version:" + registerClientAppNameMessage.getVersion());

        ConnectionMessage connectionInfo = new ConnectionMessage();
        connectionInfo.setClientAppName(registerClientAppNameMessage.getClientAppName());
        connectionInfo.setClientIp(ipAndPort.substring(0, ipAndPort.indexOf(':')));
        connectionInfo.setClientIpAndPort(ipAndPort);
        connectionInfo.setConnected(true);
        serverMessageListener.onMessage(rpcMessage.getId(), connectionInfo.getClientIp(),
            connectionInfo.getClientAppName(), connectionInfo);
        return;
      }

      if (rpcMessage.getBody() == HeartbeatMessage.PING) {
        sendResponse(rpcMessage.getId(), ctx.channel(), HeartbeatMessage.PONG);
        if (logger.isDebugEnabled()) {
          logger.debug("received PING from " + ctx.channel().remoteAddress());
        }
        return;
      }
    }

    super.channelRead(ctx, msg);
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
