package io.dts.client;

import io.dts.client.exception.DtsTransactionException;
import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.GlobalRollbackResultMessage;

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
      BeginResultMessage beginResultMessage = dtsClient.invoke(beginMessage, timeout);
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
      GlobalCommitResultMessage commitResultMessage = dtsClient.invoke(commitMessage, getTimeout());
      if (commitResultMessage != null) {
        DtsContext.unbind();
      } else {
        throw new DtsTransactionException("commit response is null");
      }
    } catch (DtsException e) {
      throw new DtsTransactionException("transaction commit fail", e);
    }
  }

  private long getTimeout() {
    return 30000l;
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
      GlobalRollbackResultMessage rollbackResultMessage = dtsClient.invoke(rollbackMessage, getTimeout());
      if (rollbackResultMessage != null) {
        DtsContext.unbind();
      } else {
        throw new DtsTransactionException("rollback response is null");
      }
    } catch (DtsException e) {
      throw new DtsTransactionException("transaction rollback fail", e);
    }
  }

}
