package io.dts.resourcemanager.transport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.Collections;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.DtsMessage;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.RequestHeaderMessage;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.RemotingClient;
import io.dts.remoting.exception.RemotingCommandException;
import io.dts.remoting.exception.RemotingConnectException;
import io.dts.remoting.exception.RemotingSendRequestException;
import io.dts.remoting.exception.RemotingTimeoutException;
import io.dts.remoting.netty.NettyClientConfig;
import io.dts.remoting.netty.NettyRemotingClient;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;
import io.dts.remoting.protocol.RemotingSysResponseCode;

/**
 * Created by guoyubo on 2017/9/13.
 */
public class DtsClientMessageSenderImpl implements DtsClientMessageSender {

  private RemotingClient remotingClient;

  public DtsClientMessageSenderImpl(NettyClientConfig nettyClientConfig) {
    this.remotingClient = new NettyRemotingClient(nettyClientConfig);
    this.remotingClient.updateNameServerAddressList(Collections.singletonList("127.0.0.1:10086"));
  }

  @PostConstruct
  public void init() {
    remotingClient.start();
  }

  @PreDestroy
  public void destroy() {
    remotingClient.shutdown();
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
          request = RemotingCommand.createRequestCommand(RequestCode.HEADER_REQUEST, (RequestHeaderMessage)msg);
          break;
        case RequestCode.BODY_REQUEST:
          request = RemotingCommand.createRequestCommand(RequestCode.HEADER_REQUEST, null);
          request.setBody(RemotingSerializable.encode(msg));
          break;
        default:
          throw new DtsException("unsupport request code " + requestCode);
      }

      RemotingCommand remotingCommand = remotingClient.invokeSync(serverAddress, request, timeout);
      switch (((RemotingCommand) msg).getCode()) {
        case RequestCode.HEADER_REQUEST:
          if (remotingCommand.getCode() == RemotingSysResponseCode.SUCCESS) {
            return (T) remotingCommand.decodeCommandCustomHeader(CommandCustomHeader.class);
          }
          break;
        case RequestCode.BODY_REQUEST:
          final byte[] body = remotingCommand.getBody();
          if (remotingCommand.getCode() == RemotingSysResponseCode.SUCCESS) {
            return  (T) RemotingSerializable.decode(body, DtsMessage.class);
          }
          break;
        default:
          break;
      }
      return null;
    } catch (InterruptedException e) {
      throw new DtsException(e, "internal error");
    } catch (RemotingConnectException e) {
      throw new DtsException(e, "connect remote server error");
    } catch (RemotingSendRequestException e) {
      throw new DtsException(e, "send request error");
    } catch (RemotingTimeoutException e) {
      throw new DtsException(e, "remote timeout");
    } catch (RemotingCommandException e) {
      throw new DtsException(e, "decode response error");
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
