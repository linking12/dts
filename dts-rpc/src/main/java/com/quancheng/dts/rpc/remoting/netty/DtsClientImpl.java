package com.quancheng.dts.rpc.remoting.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.ResponseCode;
import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.rpc.cluster.AddressManager;
import com.quancheng.dts.rpc.cluster.ZookeeperAddressManager;
import com.quancheng.dts.rpc.remoting.CommandCustomHeader;
import com.quancheng.dts.rpc.remoting.DtsClient;
import com.quancheng.dts.rpc.remoting.DtsInvokeCallBack;
import com.quancheng.dts.rpc.remoting.InvokeCallback;
import com.quancheng.dts.rpc.remoting.RPCHook;
import com.quancheng.dts.rpc.remoting.RemotingClient;
import com.quancheng.dts.rpc.remoting.exception.RemotingCommandException;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by guoyubo on 2017/8/24.
 */
public class DtsClientImpl implements DtsClient {

  private static final Logger log = LoggerFactory.getLogger(DtsClientImpl.class);
  public static final String DEFAULT_ZK_ADDRESS = "localhost:2181";
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
      addressManager = new ZookeeperAddressManager(DEFAULT_ZK_ADDRESS, DTS_REGISTER_ROOT_PATH);
    }
    remotingClient.setGroup(group);
    remotingClient.setAddressManager(addressManager);
    remotingClient.start();
  }

  @Override
  @PreDestroy
  public void shutdown() {
    remotingClient.shutdown();
  }

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

  public void setAddressManager(final AddressManager addressManager) {
    this.addressManager = addressManager;
  }

  public void setGroup(final String group) {
    this.group = group;
  }

  public void setAppName(final String appName) {
    this.appName = appName;
  }
}
