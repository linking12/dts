package io.dts.client;

import io.dts.client.exception.DtsTransactionException;
import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.GlobalRollbackResultMessage;
import io.dts.remoting.netty.NettyClientConfig;
import io.dts.resourcemanager.remoting.DtsRemotingClient;
import io.dts.resourcemanager.remoting.sender.DtsClientMessageSenderImpl;

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
    try {
      BeginResultMessage beginResultMessage = dtsClient.invoke(RequestCode.HEADER_REQUEST, beginMessage, timeout);
      System.out.println(beginResultMessage);
      if (beginResultMessage != null) {
        DtsContext.bind(beginResultMessage.getXid(), beginResultMessage.getNextSvrAddr());
      } else {
        throw new DtsTransactionException("begin response is null");
      }
    } catch (DtsException e) {
      throw new DtsTransactionException("request remote sever error", e);
    }
  }

  @Override
  public void commit() throws DtsException {
    this.commit(0);
  }

  @Override
  public void commit(final int retryTimes) throws DtsTransactionException {
    GlobalCommitMessage commitMessage = new GlobalCommitMessage();
    commitMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));
    try {
      GlobalCommitResultMessage commitResultMessage = dtsClient.invoke(RequestCode.HEADER_REQUEST, commitMessage, 3000l);
      if (commitResultMessage != null) {
        DtsContext.unbind();
      } else {
        throw new DtsTransactionException("commit response is null");
      }
      throw new DtsTransactionException("transaction commit fail");
    } catch (DtsException e) {
      throw new DtsTransactionException("transaction commit fail", e);
    }
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
    try {
      GlobalRollbackResultMessage rollbackResultMessage = dtsClient.invoke(RequestCode.HEADER_REQUEST, rollbackMessage, 3000l);
      if (rollbackResultMessage != null) {
        DtsContext.unbind();
      } else {
        throw new DtsTransactionException("rollback response is null");
      }
      throw new DtsTransactionException("transaction rollback fail");
    } catch (DtsException e) {
      throw new DtsTransactionException("transaction rollback fail", e);
    }
  }

  public static void main(String[] args) {
    NettyClientConfig nettyClientConfig = new NettyClientConfig();
    nettyClientConfig.setConnectTimeoutMillis(3000);
    DtsRemotingClient dtsClient = new DtsRemotingClient(nettyClientConfig);
//    dtsClient.setAddressManager(new ZookeeperAddressManager("localhost:2181", "/dts"));
//    dtsClient.setGroup("Default");
//    dtsClient.setAppName("Demo");
    dtsClient.init();
    DtsClientMessageSenderImpl clientMessageSender = new DtsClientMessageSenderImpl(dtsClient);

    try {
      DefaultDtsTransactionManager transactionManager = new DefaultDtsTransactionManager(clientMessageSender);
      transactionManager.begin(3000L);
      System.out.println(DtsContext.getCurrentXid());
    } catch (DtsTransactionException e) {
      e.printStackTrace();
    } finally {
      dtsClient.destroy();
    }
  }
}
