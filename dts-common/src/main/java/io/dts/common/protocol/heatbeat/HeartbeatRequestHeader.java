package io.dts.common.protocol.heatbeat;

import io.dts.common.protocol.RequestMessage;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.exception.RemotingCommandException;


public class HeartbeatRequestHeader implements CommandCustomHeader, RequestMessage {

  @Override
  public void checkFields() throws RemotingCommandException {}

}
