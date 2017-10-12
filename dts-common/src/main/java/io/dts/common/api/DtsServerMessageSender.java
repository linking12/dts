package io.dts.common.api;

import io.dts.common.common.exception.DtsException;
import io.dts.common.protocol.RequestMessage;

/**
 * server调用client端接口
 * 
 * @author hanjie
 * 
 */
public interface DtsServerMessageSender extends BaseMessageSender {

  public <T> T invokeSync(String clientAddress, RequestMessage msg, long timeout)
      throws DtsException;

  public void invokeAsync(String clientAddress, RequestMessage msg, long timeout)
      throws DtsException;

}
