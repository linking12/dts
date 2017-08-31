package org.dts.client;

import org.dts.client.remoting.DtsClient;
import org.dts.client.remoting.DtsClientImpl;

import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.RequestCode;
import com.quancheng.dts.common.DtsContext;
import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.message.request.TransactionBeginMessage;
import com.quancheng.dts.message.response.TransactionBeginBody;
import com.quancheng.dts.rpc.remoting.CommandCustomHeader;
import com.quancheng.dts.rpc.remoting.netty.NettyClientConfig;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

/**
 * Created by guoyubo on 2017/8/24.
 */
public class DefaultDtsTransactionManager implements DtsTransactionManager {

  private DtsClient dtsClient;

  public DefaultDtsTransactionManager(final DtsClient dtsClient) {
    this.dtsClient = dtsClient;
  }

  @Override
  public void begin(final long timeout) throws DtsException {
    final CommandCustomHeader requestHeader = null;
    RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.TRANSACTION_BEGIN, requestHeader);
    final TransactionBeginMessage beginMessage = new TransactionBeginMessage();
    request.setBody(RemotingSerializable.encode(beginMessage));
    TransactionBeginBody transactionBeginBody = dtsClient.invokeSync(request, TransactionBeginBody.class);
    DtsContext.bind(transactionBeginBody.getXid(), transactionBeginBody.getNextServerAddr());
  }

  @Override
  public void commit() throws DtsException {

  }

  @Override
  public void commit(final int retryTimes) throws DtsException {
    dtsClient.commit(retryTimes);
  }

  @Override
  public void rollback() throws DtsException {
    dtsClient.rollback(0);
  }

  @Override
  public void rollback(final int retryTimes) throws DtsException {
    dtsClient.rollback(retryTimes);
  }

  public static void main(String[] args) throws DtsException {
    NettyClientConfig nettyClientConfig = new NettyClientConfig();
    nettyClientConfig.setConnectTimeoutMillis(3000);
    DtsClient dtsClient = new DtsClientImpl(nettyClientConfig);
    dtsClient.start();
    DefaultDtsTransactionManager transactionManager = new DefaultDtsTransactionManager(dtsClient);
    transactionManager.begin(3000L);
    System.out.println(DtsContext.getCurrentXid());
    dtsClient.shutdown();
  }
}
