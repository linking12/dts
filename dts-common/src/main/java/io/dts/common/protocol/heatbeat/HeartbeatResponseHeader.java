package io.dts.common.protocol.heatbeat;

import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.exception.RemotingCommandException;

/**
 * Created by cn40387 on 15/3/6.
 */
public class HeartbeatResponseHeader implements CommandCustomHeader {
  @Override
  public void checkFields() throws RemotingCommandException {

  }
}
