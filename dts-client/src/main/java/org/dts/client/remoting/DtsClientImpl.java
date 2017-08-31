package org.dts.client.remoting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.RequestCode;
import com.quancheng.dts.ResponseCode;
import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.message.response.TransactionBeginBody;
import com.quancheng.dts.rpc.cluster.AddressManager;
import com.quancheng.dts.rpc.cluster.ZookeeperAddressManager;
import com.quancheng.dts.rpc.remoting.CommandCustomHeader;
import com.quancheng.dts.rpc.remoting.InvokeCallback;
import com.quancheng.dts.rpc.remoting.RemotingClient;
import com.quancheng.dts.rpc.remoting.exception.RemotingConnectException;
import com.quancheng.dts.rpc.remoting.exception.RemotingSendRequestException;
import com.quancheng.dts.rpc.remoting.exception.RemotingTimeoutException;
import com.quancheng.dts.rpc.remoting.netty.NettyClientConfig;
import com.quancheng.dts.rpc.remoting.netty.NettyRemotingClient;
import com.quancheng.dts.rpc.remoting.netty.ResponseFuture;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by guoyubo on 2017/8/24.
 */
public class DtsClientImpl implements DtsClient {

  private static final Logger log = LoggerFactory.getLogger(DtsClientImpl.class);
  private RemotingClient remotingClient;
  private List<String> serverAddressList;
  private long timeoutMillis = 3000;
  private AddressManager addressManager = new ZookeeperAddressManager("localhost:2181", "/dts");
  private String group;

  public DtsClientImpl(NettyClientConfig nettyClientConfig) {

    remotingClient = new NettyRemotingClient(nettyClientConfig);
    remotingClient.setAddressManager(addressManager);
//    this.clientMessageSender = new RpcClient(new ThreadPoolExecutor(1, 20, 0,
//        TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>()));
//    this.clientMessageSender.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
//    this.clientMessageSender.init();
  }

  @Override
  @PostConstruct
  public void start() {
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
  public <T> T invokeSync(RemotingCommand request, Class<T> classOfT) throws DtsException {

    try {
      RemotingCommand response = remotingClient.invokeSync(null, request, timeoutMillis);
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


  @Override
  public <T> void invokeAsync(RemotingCommand request, Class<T> classOfT, DtsInvokeCallBack<T> dtsInvokeCallBack) throws DtsException {

    try {
      remotingClient.invokeAsync(null, request, timeoutMillis, new InvokeCallback() {
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

  public TransactionBeginBody begin(final long timeout) throws DtsException {

    final CommandCustomHeader requestHeader = null;
    RemotingCommand request =
        RemotingCommand.createRequestCommand(RequestCode.TRANSACTION_BEGIN, requestHeader);
    RemotingCommand response = null;
    try {
      response = remotingClient.invokeSync(new InetSocketAddress(6666).getHostString()+":6666", request, 3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (RemotingConnectException e) {
      e.printStackTrace();
    } catch (RemotingSendRequestException e) {
      e.printStackTrace();
    } catch (RemotingTimeoutException e) {
      e.printStackTrace();
    }
    if (response != null) {
      switch (response.getCode()) {
        case ResponseCode.SUCCESS: {
          TransactionBeginBody transactionBeginBody =
              TransactionBeginBody.decode(response.getBody(), TransactionBeginBody.class);
          System.out.println(transactionBeginBody.getXid());
        }
        default:
          break;
      }
    }
    return null;
  }

  public void setAddressManager(final AddressManager addressManager) {
    this.addressManager = addressManager;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(final String group) {
    this.group = group;
  }
}
