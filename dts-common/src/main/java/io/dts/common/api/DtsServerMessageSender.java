package io.dts.common.api;

import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestMessage;

/**
 * server调用client端接口
 * 
 */
public interface DtsServerMessageSender extends BaseMessageSender {

  public <T> T invokeSync(String clientAddress, String clientKey, RequestMessage msg, long timeout)
      throws DtsException;

  public void invokeAsync(String clientAddress, String clientKey, RequestMessage msg, long timeout)
      throws DtsException;

}
