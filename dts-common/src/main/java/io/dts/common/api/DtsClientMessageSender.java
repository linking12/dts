package io.dts.common.api;

import io.dts.common.exception.DtsException;

/**
 * client同步调用server端接口
 * 
 * @author hanjie
 *
 */
public interface DtsClientMessageSender {

  public <T> T invoke(Object msg, long timeout) throws DtsException;

  public <T> T invoke(String serverAddress, Object msg, long timeout) throws DtsException;

  public <T> T invoke(Object msg) throws DtsException;

  public void sendResponse(long msgId, String serverAddress, Object msg);
}
