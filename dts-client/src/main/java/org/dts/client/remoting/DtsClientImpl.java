package org.dts.client.remoting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.ResponseCode;
import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.rpc.cluster.AddressManager;
import com.quancheng.dts.rpc.cluster.ZookeeperAddressManager;
import com.quancheng.dts.rpc.remoting.CommandCustomHeader;
import com.quancheng.dts.rpc.remoting.InvokeCallback;
import com.quancheng.dts.rpc.remoting.RPCHook;
import com.quancheng.dts.rpc.remoting.RemotingClient;
import com.quancheng.dts.rpc.remoting.exception.RemotingCommandException;
import com.quancheng.dts.rpc.remoting.netty.NettyClientConfig;
import com.quancheng.dts.rpc.remoting.netty.NettyRemotingClient;
import com.quancheng.dts.rpc.remoting.netty.ResponseFuture;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;
import com.quancheng.dts.rpc.util.NetUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by guoyubo on 2017/8/24.
 */
public class DtsClientImpl implements DtsClient {

  private static final Logger log = LoggerFactory.getLogger(DtsClientImpl.class);
  private RemotingClient remotingClient;
  private long timeoutMillis = 3000;
  private AddressManager addressManager;
  private String appName = "client";
  private String group = "DEFAULT";
  public static final String DTS_REGISTER_ROOT_PATH = "/dts";


  public DtsClientImpl(NettyClientConfig nettyClientConfig) {

    remotingClient = new NettyRemotingClient(nettyClientConfig);
    remotingClient.registerRPCHook(new RPCHook() {
      @Override
      public void doBeforeRequest(final String remoteAddr, final RemotingCommand request) {
        CommandCustomHeader customHeader = request.readCustomHeader();
        if (customHeader == null) {
          request.writeCustomHeader(new CommandCustomHeader() {
            @Override
            public void checkFields() throws RemotingCommandException {

            }
          });
        }
        request.addExtField("appName", appName);
        request.addExtField("serverGroup", group);
        request.addExtField("appAddress", NetUtil.getLocalIp());
      }

      @Override
      public void doAfterResponse(final String remoteAddr, final RemotingCommand request,
          final RemotingCommand response) {

      }
    });
  }

  @Override
  @PostConstruct
  public void start() {
    if (addressManager == null) {
      addressManager = new ZookeeperAddressManager("localhost:2181", DTS_REGISTER_ROOT_PATH);
    }
    remotingClient.setAddressManager(addressManager);
    remotingClient.start();
  }

  @Override
  @PreDestroy
  public void shutdown() {
    remotingClient.shutdown();
  }

  @Override
  public void setTimeoutMillis(final long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }

  @Override
  public <T> T invokeSync(RemotingCommand request, Long timeoutMillis, Class<T> classOfT) throws DtsException {

    try {
      RemotingCommand response = remotingClient.invokeSync(null, request, determineTimeoutMillis(timeoutMillis));
      if (response != null) {
        switch (response.getCode()) {
          case ResponseCode.SUCCESS:
            return RemotingSerializable.decode(response.getBody(), classOfT);
          default:
            break;
        }
      }
      return null;
    } catch (Exception e) {
      log.error("invokeSync error", e);
      throw new DtsException(e);
    }
  }

  private long determineTimeoutMillis(final Long timeoutMillis) {
    return timeoutMillis != null ?  timeoutMillis : this.timeoutMillis;
  }

  @Override
  public <T> void invokeAsync(RemotingCommand request, Long timeoutMillis, Class<T> classOfT, DtsInvokeCallBack<T> dtsInvokeCallBack) throws DtsException {

    try {
      remotingClient.invokeAsync(null, request, determineTimeoutMillis(timeoutMillis), new InvokeCallback() {
        @Override
        public void operationComplete(final ResponseFuture responseFuture) {
          RemotingCommand response = responseFuture.getResponseCommand();
          if (response != null) {
            switch (response.getCode()) {
              case ResponseCode.SUCCESS:
                dtsInvokeCallBack.execute(RemotingSerializable.decode(response.getBody(), classOfT));
              default:
                break;
            }
          }
        }
      });
    } catch (Exception e) {
      log.error("invokeSync error", e);
      throw new DtsException(e);
    }
  }

//  public TransactionBeginBody begin(final long timeout) throws DtsException {
//
//    final CommandCustomHeader requestHeader = null;
//    RemotingCommand request =
//        RemotingCommand.createRequestCommand(RequestCode.TRANSACTION_BEGIN, requestHeader);
//    RemotingCommand response = null;
//    try {
//      response = remotingClient.invokeSync(new InetSocketAddress(6666).getHostString()+":6666", request, 3000);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    } catch (RemotingConnectException e) {
//      e.printStackTrace();
//    } catch (RemotingSendRequestException e) {
//      e.printStackTrace();
//    } catch (RemotingTimeoutException e) {
//      e.printStackTrace();
//    }
//    if (response != null) {
//      switch (response.getCode()) {
//        case ResponseCode.SUCCESS: {
//          TransactionBeginBody transactionBeginBody =
//              TransactionBeginBody.decode(response.getBody(), TransactionBeginBody.class);
//          System.out.println(transactionBeginBody.getXid());
//        }
//        default:
//          break;
//      }
//    }
//    return null;
//  }

  @Override
  public void setAddressManager(final AddressManager addressManager) {
    this.addressManager = addressManager;
  }

  public void setGroup(final String group) {
    this.group = group;
  }

  @Override
  public void setAppName(final String appName) {
    this.appName = appName;
  }
}
