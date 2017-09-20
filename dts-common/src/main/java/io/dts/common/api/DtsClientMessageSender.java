package io.dts.common.api;

import io.dts.common.exception.DtsException;
import io.dts.common.protocol.DtsMessage;

/**
 * client同步调用server端接口
 * 
 * @author hanjie
 *
 */
public interface DtsClientMessageSender {

  public <T> T invoke(int requestCode, DtsMessage msg, long timeout) throws DtsException;

  public <T> T invoke(String serverAddress, int requestCode, DtsMessage msg, long timeout)
      throws DtsException;

  public <T> T invoke(int requestCode, DtsMessage msg) throws DtsException;

}
