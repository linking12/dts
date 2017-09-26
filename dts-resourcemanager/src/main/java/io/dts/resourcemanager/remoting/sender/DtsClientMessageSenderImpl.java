package io.dts.resourcemanager.remoting.sender;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestMessage;
import io.dts.remoting.exception.RemotingCommandException;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.resourcemanager.remoting.DtsRemotingClient;

/**
 * Created by guoyubo on 2017/9/13.
 */
public class DtsClientMessageSenderImpl implements DtsClientMessageSender {

  private DtsRemotingClient remotingClient;

  public DtsClientMessageSenderImpl( DtsRemotingClient remotingClient) {
    this.remotingClient = remotingClient;
  }

  @Override
  public <T> T invoke(RequestMessage msg, long timeout) throws DtsException {
    if (DtsContext.getCurrentXid() != null) {
      return this.invoke(TxcXID.getServerAddress(DtsContext.getCurrentXid()), msg, timeout);
    } else {
      return this.invoke(null, msg, timeout);
    }
  }

  @Override
  public <T> T invoke(String serverAddress, RequestMessage msg, long timeout) throws DtsException {
    try {
      RemotingCommand request = this.buildRequest(msg);
      RemotingCommand response = remotingClient.invokeSync(serverAddress, request, timeout);
      return this.buildResponse(response);
    } catch (RemotingCommandException e) {
      throw new DtsException(e);
    }
  }

  @Override
  public <T> T invoke(RequestMessage msg) throws DtsException {
    return this.invoke(msg, 3000l);
  }


}
