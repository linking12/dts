package io.dts.client;

import io.dts.client.exception.DtsTransactionException;
import io.dts.client.transport.DtsClientMessageSenderImpl;
import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.DtsMessage;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.GlobalRollbackResultMessage;
import io.dts.remoting.netty.NettyClientConfig;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;

/**
 * Created by guoyubo on 2017/8/24.
 */
public class DefaultDtsTransactionManager implements DtsTransactionManager {

  private DtsClientMessageSender dtsClient;

  public DefaultDtsTransactionManager(final DtsClientMessageSender dtsClient) {
    this.dtsClient = dtsClient;
  }

  @Override
  public void begin(final long timeout) throws DtsTransactionException {
    final BeginMessage beginMessage = new BeginMessage();
    beginMessage.setTimeout(timeout);
    RemotingCommand request = RemotingCommand.createRequestCommand(DtsMessage.TYPE_BEGIN, beginMessage);
    BeginResultMessage beginResultMessage = null;
    try {
      beginResultMessage = (BeginResultMessage) dtsClient.invoke(request, timeout);
    } catch (DtsException e) {
      e.printStackTrace();
    }
    DtsContext.bind(beginResultMessage.getXid(), beginResultMessage.getNextSvrAddr());
  }

  @Override
  public void commit() throws DtsException {
    this.commit(0);
  }

  @Override
  public void commit(final int retryTimes) throws DtsTransactionException {
    GlobalCommitMessage commitMessage = new GlobalCommitMessage();
    commitMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));
    RemotingCommand request = RemotingCommand.createRequestCommand(DtsMessage.TYPE_GLOBAL_COMMIT, commitMessage);
    request.setBody(RemotingSerializable.encode(commitMessage));
    GlobalCommitResultMessage commitResultMessage = null;
    try {
      commitResultMessage = (GlobalCommitResultMessage) dtsClient.invoke(request, 3000l);
    } catch (DtsException e) {
      e.printStackTrace();
    }
    System.out.println(commitResultMessage.getTranId());
    DtsContext.unbind();
  }

  @Override
  public void rollback() throws DtsException {
    this.rollback(0);
  }

  @Override
  public void rollback(final int retryTimes) throws DtsTransactionException {
    GlobalRollbackMessage rollbackMessage = new GlobalRollbackMessage();
    rollbackMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));
    rollbackMessage.setRealSvrAddr(TxcXID.getServerAddress(DtsContext.getCurrentXid()));

    RemotingCommand request = RemotingCommand.createRequestCommand(DtsMessage.TYPE_GLOBAL_ROLLBACK, rollbackMessage);
    request.setBody(RemotingSerializable.encode(rollbackMessage));
    GlobalRollbackResultMessage rollbackResultMessage = null;
    try {
      rollbackResultMessage = (GlobalRollbackResultMessage) dtsClient.invoke(request);
    } catch (DtsException e) {
      e.printStackTrace();
    }
    System.out.println(rollbackResultMessage.getTranId());
    DtsContext.unbind();
  }

  public static void main(String[] args) throws DtsTransactionException {
    NettyClientConfig nettyClientConfig = new NettyClientConfig();
    nettyClientConfig.setConnectTimeoutMillis(3000);
    DtsClientMessageSenderImpl dtsClient = new DtsClientMessageSenderImpl(nettyClientConfig);
//    dtsClient.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
//    dtsClient.setGroup("Default");
//    dtsClient.setAppName("Demo");
    dtsClient.init();
    DefaultDtsTransactionManager transactionManager = new DefaultDtsTransactionManager(dtsClient);
    transactionManager.begin(3000L);
    System.out.println(DtsContext.getCurrentXid());
    dtsClient.destroy();
  }
}
