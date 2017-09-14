package io.dts.resourcemanager.transport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.DtsMessage;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.RequestHeaderMessage;
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

  public DtsClientMessageSenderImpl() {
    final NettyClientConfig nettyClientConfig = new NettyClientConfig();
    this.remotingClient = new NettyRemotingClient(nettyClientConfig);
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
  public <T> T invoke(final Object msg, final long timeout) throws DtsException {
    return this.invoke(TxcXID.getServerAddress(DtsContext.getCurrentXid()), msg, timeout);
  }

  @Override
  public <T> T invoke(final String serverAddress, final Object msg, final long timeout)
      throws DtsException {
    try {
      RemotingCommand remotingCommand = remotingClient.invokeSync(serverAddress, (RemotingCommand) msg, timeout);
      switch (((RemotingCommand) msg).getCode()) {
        case RequestCode.HEADER_REQUEST:
          if (remotingCommand.getCode() == RemotingSysResponseCode.SUCCESS) {
            return (T) remotingCommand.decodeCommandCustomHeader(RequestHeaderMessage.class);
          }
        case RequestCode.BODY_REQUEST:
          final byte[] body = remotingCommand.getBody();
          if (remotingCommand.getCode() == RemotingSysResponseCode.SUCCESS) {
            return  (T) RemotingSerializable.decode(body, DtsMessage.class);
          }
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
  public <T> T invoke(final Object msg) throws DtsException {
    return this.invoke(msg, 3000l);
  }

  @Override
  public void sendResponse(final long msgId, final String serverAddress, final Object msg) {
  }
}
