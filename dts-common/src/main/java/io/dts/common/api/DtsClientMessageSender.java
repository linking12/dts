package io.dts.common.api;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * client同步调用server端接口
 * 
 * @author hanjie
 *
 */
public interface DtsClientMessageSender {

  public Object invoke(Object msg, long timeout) throws IOException, TimeoutException;

  public Object invoke(String serverAddress, Object msg, long timeout)
      throws IOException, TimeoutException;

  public Object invoke(Object msg) throws IOException, TimeoutException;

  public void sendResponse(long msgId, String serverAddress, Object msg);
}
