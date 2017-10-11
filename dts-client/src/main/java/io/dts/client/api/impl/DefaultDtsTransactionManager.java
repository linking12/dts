package io.dts.client.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.client.aop.TransactionDtsInterceptor;
import io.dts.client.api.DtsTransactionManager;
import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.TxcConstants;
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

  private static final Logger logger = LoggerFactory.getLogger(TransactionDtsInterceptor.class);

  private DtsClientMessageSender dtsClient;

  public DefaultDtsTransactionManager(final DtsClientMessageSender dtsClient) {
    this.dtsClient = dtsClient;
  }

  @Override
  public void begin(final long timeout) throws DtsException {
    if (DtsContext.inRetryContext()) {
      throw new DtsException("This transaction has been RT model!");
    }
    int beginCount = DtsContext.getBeginCount();
    DtsContext.setBegin(++beginCount);
    if (beginCount == 1) {
      BeginMessage beginMessage = new BeginMessage();
      beginMessage.setTimeout(timeout);
      long start = 0;
      if (logger.isDebugEnabled())
        start = System.currentTimeMillis();
      BeginResultMessage resultMessage = null;
      try {
        BeginResultMessage beginResultMessage =
            dtsClient.invoke(beginMessage, TxcConstants.RPC_INVOKE_TIMEOUT);
        DtsContext.bind(beginResultMessage.getXid(), beginResultMessage.getNextSvrAddr());
      } catch (Throwable th) {
        throw new DtsException(th);
      } finally {
        if (logger.isDebugEnabled()) {
          long end = System.currentTimeMillis();
          logger.debug(resultMessage + " cost " + (end - start) + " ms.");
        } else
          logger.info("begin transaction. " + resultMessage);
      }
    } else {
      logger.info(String.format("merge transaction , level %s", beginCount));
    }
  }

  @Override
  public void commit() throws DtsException {
    this.commit(0);
  }

  @Override
  public void commit(int retryTimes) throws DtsException {
    GlobalCommitMessage commitMessage = new GlobalCommitMessage();
    if (DtsContext.getCurrentXid() == null) {
      throw new DtsException("the thread is not in transaction when invoke commit.");
    }
    commitMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));
    long start = 0;
    if (logger.isDebugEnabled())
      start = System.currentTimeMillis();
    try {
      GlobalCommitResultMessage resultMessage = null;
      Exception ex = null;
      do {
        try {
          resultMessage = (GlobalCommitResultMessage) dtsClient.invoke(commitMessage,
              TxcConstants.RPC_INVOKE_TIMEOUT);
          Thread.sleep(3000);
        } catch (Exception e) {
          ex = e;
          try {
            Thread.sleep(3000);
          } catch (InterruptedException e1) {
          }
        }
      } while (retryTimes-- > 0);
      if (resultMessage == null) {
        throw new DtsException("transaction " + DtsContext.getCurrentXid()
            + " Global commit failed.server response is null");
      }
      if (ex != null) {
        throw new DtsException(ex,
            "transaction " + DtsContext.getCurrentXid() + " Global commit failed.");
      }
    } finally {
      DtsContext.unbind();
      if (logger.isDebugEnabled()) {
        long end = System.currentTimeMillis();
        logger.debug(
            "send global commit message:" + commitMessage + " cost " + (end - start) + " ms.");
      } else
        logger.info("send global commit message:" + commitMessage);
    }
  }

  @Override
  public void rollback() throws DtsException {
    this.rollback(0);
  }

  @Override
  public void rollback(int retryTimes) throws DtsException {
    GlobalRollbackMessage rollbackMessage = new GlobalRollbackMessage();
    rollbackMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));
    long start = 0;
    if (logger.isDebugEnabled())
      start = System.currentTimeMillis();
    GlobalRollbackResultMessage resultMessage = null;
    try {
      Exception ex = null;
      do {
        try {
          resultMessage = (GlobalRollbackResultMessage) dtsClient.invoke(rollbackMessage,
              TxcConstants.RPC_INVOKE_TIMEOUT);
          Thread.sleep(3000);
        } catch (Exception e) {
          ex = e;
          try {
            Thread.sleep(3000);
          } catch (InterruptedException e1) {
          }
        }
      } while (retryTimes-- > 0);
      if (resultMessage == null) {
        throw new DtsException("transaction " + DtsContext.getCurrentXid()
            + " Global rollback failed.server response is null");
      }
      if (ex != null) {
        throw new DtsException(ex,
            "transaction " + DtsContext.getCurrentXid() + " Global rollback failed.");
      }
    } finally {
      DtsContext.unbind();
      if (logger.isDebugEnabled()) {
        long end = System.currentTimeMillis();
        logger.debug("invoke global rollback message:" + rollbackMessage + " cost " + (end - start)
            + " ms.");
      }
    }
  }

}
