package io.dts.common.protocol.heatbeat;

import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.exception.RemotingCommandException;


public class HeartbeatRequestHeader implements CommandCustomHeader {
  private String clientId;

  @Override
  public void checkFields() throws RemotingCommandException {}

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
}
