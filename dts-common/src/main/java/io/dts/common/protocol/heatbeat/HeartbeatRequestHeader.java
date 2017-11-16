package io.dts.common.protocol.heatbeat;

import io.dts.common.protocol.RequestMessage;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.exception.RemotingCommandException;


public class HeartbeatRequestHeader implements CommandCustomHeader, RequestMessage {

  private String clientOrResourceInfo;

  public String getClientOrResourceInfo() {
    return clientOrResourceInfo;
  }

  public void setClientOrResourceInfo(String dbName) {
    this.clientOrResourceInfo = dbName;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}

}
