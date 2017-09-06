package io.dts.common.api;

import io.netty.channel.Channel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * server调用client端接口
 * 
 * @author hanjie
 * 
 */
public interface DtsServerMessageSender {

  /**
   * 异步调用client
   * 
   * @param dbKey
   * @param clientIp
   * @param clientAppName
   * @param msg
   */
  public void sendRequest(String dbKey, String clientIp, String clientAppName, Object msg);

  /**
   * 异步调用RT模式client
   * 
   * @param dbKey
   * @param clientIp
   * @param clientAppName
   * @param msg
   */
  public void sendRtRequest(String dbKey, String clientIp, String clientAppName, Object msg);

  /**
   * @param msgId
   * @param dbKey
   * @param clientIp
   * @param clientAppName
   * @param msg
   */
  public void sendResponse(long msgId, String dbKey, String clientIp, String clientAppName,
      Object msg);

  /**
   * @param msgId
   * @param channel
   * @param msg
   */
  public void sendResponse(long msgId, Channel channel, Object msg);

  /**
   * 同步调用client
   * 
   * @param dbKey
   * @param clientIp
   * @param clientAppName
   * @param msg
   * @return
   * @throws IOException
   */
  public Object invoke(String dbKey, String clientIp, String clientAppName, Object msg,
      long timeout) throws IOException, TimeoutException;

  /**
   * 同步调用client
   * 
   * @param dbKey
   * @param clientIp
   * @param clientAppName
   * @param msg
   * @return
   * @throws IOException
   */
  public Object invoke(String dbKey, String clientIp, String clientAppName, Object msg)
      throws IOException, TimeoutException;
}
