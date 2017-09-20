package io.dts.common.api;

import io.dts.common.exception.DtsException;
import io.dts.common.protocol.DtsMessage;

/**
 * server调用client端接口
 * 
 * @author hanjie
 * 
 */
public interface DtsServerMessageSender {

  public <T> T invokeSync(String clientAddress, DtsMessage msg, long timeout) throws DtsException;

  public void invokeAsync(String clientAddress, DtsMessage msg, long timeout) throws DtsException;

}
