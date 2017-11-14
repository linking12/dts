package io.dts.common.api;

import io.dts.common.cluster.ServerCluster;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestMessage;

/**
 * client同步调用server端接口
 * 
 */
public interface DtsClientMessageSender extends BaseMessageSender {

  public <T> T invoke(RequestMessage msg, long timeout) throws DtsException;

  public <T> T invoke(String serverAddress, RequestMessage msg, long timeout) throws DtsException;

  public <T> T invoke(RequestMessage msg) throws DtsException;

  default String selectAddress() {
    return ServerCluster.getServerCluster().select();
  }

}
