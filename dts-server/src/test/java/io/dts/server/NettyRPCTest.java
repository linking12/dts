/**
 * $Id: NettyRPCTest.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package io.dts.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.dts.common.common.exception.DtsException;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.RequestMessage;
import io.dts.common.protocol.ResponseMessage;
import io.dts.common.protocol.heatbeat.HeartbeatRequestHeader;
import io.dts.common.util.thread.ThreadFactoryImpl;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.RemotingClient;
import io.dts.remoting.exception.RemotingCommandException;
import io.dts.remoting.netty.NettyClientConfig;
import io.dts.remoting.netty.NettyRemotingClient;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;
import io.dts.remoting.protocol.RemotingSysResponseCode;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class NettyRPCTest {

  public static RemotingCommand buildRequest(RequestMessage dtsMessage) throws DtsException {
    RemotingCommand request = null;
    if (dtsMessage instanceof CommandCustomHeader) {
      request = RemotingCommand.createRequestCommand(RequestCode.HEADER_REQUEST,
          (CommandCustomHeader) dtsMessage);
    } else if (dtsMessage instanceof RemotingSerializable) {
      request = RemotingCommand.createRequestCommand(RequestCode.BODY_REQUEST, null);
      request.setBody(RemotingSerializable.encode1(dtsMessage));
    } else {
      throw new DtsException("request must implements CommandCustomHeader or RemotingSerializable");
    }
    return request;
  }

  @SuppressWarnings("unchecked")
  public static <T> T buildResponse(RemotingCommand response) throws RemotingCommandException {
    if (response.getCode() == RemotingSysResponseCode.SUCCESS) {
      if (!response.getExtFields().isEmpty()) {
        return (T) response.decodeCommandCustomHeader(CommandCustomHeader.class);
      } else if (response.getBody() != null) {
        return (T) RemotingSerializable.decode(response.getBody(), ResponseMessage.class);
      }
    } else {
      throw new DtsException(response.getRemark());
    }
    return null;
  }

  public static RemotingClient createRemotingClient() {
    NettyClientConfig config = new NettyClientConfig();
    RemotingClient client = new NettyRemotingClient(config);
    client.start();
    sendHeartbeat(client);
    return client;
  }


  public static void sendHeartbeat(RemotingClient client) {
    ScheduledExecutorService scheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("Dts Heartbeat"));
    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          HeartbeatRequestHeader header = new HeartbeatRequestHeader();
          RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.HEART_BEAT,
              (CommandCustomHeader) header);
          client.invokeSync("localhost:10086", request, 1000 * 3);
        } catch (Exception e) {
          // ignore
        }
      }
    }, 0, 5, TimeUnit.SECONDS);
  }


}
