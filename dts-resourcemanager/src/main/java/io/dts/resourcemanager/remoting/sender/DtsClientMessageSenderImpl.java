package io.dts.resourcemanager.remoting.sender;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.DtsMessage;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.RequestHeaderMessage;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.exception.RemotingCommandException;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;
import io.dts.remoting.protocol.RemotingSysResponseCode;
import io.dts.resourcemanager.remoting.DtsRemotingClient;

/**
 * Created by guoyubo on 2017/9/13.
 */
public class DtsClientMessageSenderImpl implements DtsClientMessageSender {

  private DtsRemotingClient dtsRemotingClient;

  public DtsClientMessageSenderImpl(final DtsRemotingClient dtsRemotingClient) {
    this.dtsRemotingClient = dtsRemotingClient;
  }

  @Override
  public <T> T invoke(int requestCode, final DtsMessage msg, final long timeout) throws DtsException {
    if (DtsContext.getCurrentXid() != null) {
      return this.invoke(TxcXID.getServerAddress(DtsContext.getCurrentXid()), requestCode, msg, timeout);
    } else {
      return this.invoke(null, requestCode, msg, timeout);
    }
  }

  @Override
  public <T> T invoke(final String serverAddress, int requestCode, final DtsMessage msg, final long timeout)
      throws DtsException {
    try {
      RemotingCommand request;
      switch (requestCode) {
        case RequestCode.HEADER_REQUEST:
          request = RemotingCommand.createRequestCommand(RequestCode.HEADER_REQUEST, (RequestHeaderMessage) msg);
          break;
        case RequestCode.BODY_REQUEST:
          request = RemotingCommand.createRequestCommand(RequestCode.HEADER_REQUEST, null);
          request.setBody(RemotingSerializable.encode(msg));
          break;
        default:
          throw new DtsException("unsupport request code " + requestCode);
      }

      RemotingCommand remotingCommand = dtsRemotingClient.invokeSync(serverAddress, request, timeout);
      switch (((RemotingCommand) msg).getCode()) {
        case RequestCode.HEADER_REQUEST:
          if (remotingCommand.getCode() == RemotingSysResponseCode.SUCCESS) {
            return (T) remotingCommand.decodeCommandCustomHeader(CommandCustomHeader.class);
          }
          break;
        case RequestCode.BODY_REQUEST:
          final byte[] body = remotingCommand.getBody();
          if (remotingCommand.getCode() == RemotingSysResponseCode.SUCCESS) {
            return (T) RemotingSerializable.decode(body, DtsMessage.class);
          }
          break;
        default:
          break;
      }
      return null;
    } catch (RemotingCommandException e) {
      throw new DtsException(e, "decode header error");
    }
  }

    @Override
  public <T> T invoke(int requestCode, final DtsMessage msg) throws DtsException {
    return this.invoke(requestCode, msg, 3000l);
  }

  @Override
  public void sendResponse(final long msgId, final String serverAddress, final Object msg) {
  }
}
