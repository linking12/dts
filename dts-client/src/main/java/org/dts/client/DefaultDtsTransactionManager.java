package org.dts.client;

import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.RequestCode;
import com.quancheng.dts.common.DtsContext;
import com.quancheng.dts.common.DtsXID;
import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.message.request.BeginMessage;
import com.quancheng.dts.message.request.GlobalCommitMessage;
import com.quancheng.dts.message.request.GlobalRollbackMessage;
import com.quancheng.dts.message.response.BeginResultMessage;
import com.quancheng.dts.message.response.GlobalCommitResultMessage;
import com.quancheng.dts.message.response.GlobalRollbackResultMessage;
import com.quancheng.dts.rpc.remoting.CommandCustomHeader;
import com.quancheng.dts.rpc.remoting.DtsClient;
import com.quancheng.dts.rpc.remoting.netty.DtsClientImpl;
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
    final BeginMessage beginMessage = new BeginMessage();
    beginMessage.setTimeout(timeout);
    request.setBody(RemotingSerializable.encode(beginMessage));
    BeginResultMessage transactionBeginBody = dtsClient.invokeSync(request, timeout, BeginResultMessage.class);
    DtsContext.bind(transactionBeginBody.getXid(), transactionBeginBody.getNextServerAddr());
  }

  @Override
  public void commit() throws DtsException {
    this.commit(0);
  }

  @Override
  public void commit(final int retryTimes) throws DtsException {
    GlobalCommitMessage commitMessage = new GlobalCommitMessage();
    commitMessage.setTranId(DtsXID.getTransactionId(DtsContext.getCurrentXid()));
    commitMessage.setRetryTimes(retryTimes);
    RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.TRANSACTION_COMMIT, null);
    request.setBody(RemotingSerializable.encode(commitMessage));
    GlobalCommitResultMessage commitResultMessage = dtsClient.invokeSync(request,null, GlobalCommitResultMessage.class);
    System.out.println(commitResultMessage.getTranId());
    DtsContext.unbind();
  }

  @Override
  public void rollback() throws DtsException {
    this.rollback(0);
  }

  @Override
  public void rollback(final int retryTimes) throws DtsException {
    GlobalRollbackMessage rollbackMessage = new GlobalRollbackMessage();
    rollbackMessage.setTranId(DtsXID.getTransactionId(DtsContext.getCurrentXid()));
    rollbackMessage.setRetryTimes(retryTimes);

    RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.TRANSACTION_ROLLBACK, null);
    request.setBody(RemotingSerializable.encode(rollbackMessage));
    GlobalRollbackResultMessage rollbackResultMessage = dtsClient.invokeSync(request,null, GlobalRollbackResultMessage.class);
    System.out.println(rollbackResultMessage.getTranId());
    DtsContext.unbind();
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
