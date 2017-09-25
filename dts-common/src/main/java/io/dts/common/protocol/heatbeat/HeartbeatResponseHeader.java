package io.dts.common.protocol.heatbeat;

import io.dts.common.protocol.ResponseMessage;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.exception.RemotingCommandException;


public class HeartbeatResponseHeader implements CommandCustomHeader, ResponseMessage {
  @Override
  public void checkFields() throws RemotingCommandException {

  }
}
