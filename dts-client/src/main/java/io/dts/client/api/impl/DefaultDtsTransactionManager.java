package io.dts.client.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.client.aop.TransactionDtsInterceptor;
import io.dts.client.api.DtsTransactionManager;
import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.context.DtsContext;
import io.dts.common.context.DtsXID;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.GlobalRollbackResultMessage;
import io.dts.remoting.RemoteConstant;

/**
 * Created by guoyubo on 2017/8/24.
 */
public class DefaultDtsTransactionManager implements DtsTransactionManager {

  private static final Logger logger = LoggerFactory.getLogger(TransactionDtsInterceptor.class);

  private final DtsClientMessageSender dtsClient;

  private static final DtsTransactionManager transcationManager =
      new DefaultDtsTransactionManager();

  private DefaultDtsTransactionManager() {
    DefaultDtsClientMessageSender clientMessageSender = new DefaultDtsClientMessageSender();
    clientMessageSender.start();
    this.dtsClient = clientMessageSender;
  }

  public static DtsTransactionManager getInstance() {
    return transcationManager;
  }

  @Override
  public void begin(final long timeout) throws DtsException {
    BeginMessage beginMessage = new BeginMessage();
    beginMessage.setTimeout(timeout);
    try {
      BeginResultMessage beginResultMessage =
          dtsClient.invoke(beginMessage, RemoteConstant.RPC_INVOKE_TIMEOUT);
      String transId = beginResultMessage.getXid();
      DtsContext.getInstance().bind(transId);
    } catch (Throwable th) {
      throw new DtsException(th);
    }
  }

  @Override
  public void commit() throws DtsException {
    this.commit(0);
  }

  @Override
  public void commit(int retryTimes) throws DtsException {
    GlobalCommitMessage commitMessage = new GlobalCommitMessage();
    if (DtsContext.getInstance().getCurrentXid() == null) {
      throw new DtsException("the thread is not in transaction when invoke commit.");
    }
    int transId = DtsXID.getTransactionId(DtsContext.getInstance().getCurrentXid());
    commitMessage.setTranId(transId);
    long start = 0;
    if (logger.isDebugEnabled())
      start = System.currentTimeMillis();
    try {
      GlobalCommitResultMessage resultMessage = null;
      Exception ex = null;
      do {
        try {
          resultMessage = (GlobalCommitResultMessage) dtsClient.invoke(commitMessage,
              RemoteConstant.RPC_INVOKE_TIMEOUT);
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
        throw new DtsException("transaction " + DtsContext.getInstance().getCurrentXid()
            + " Global commit failed.server response is null");
      }
      if (ex != null) {
        throw new DtsException(ex,
            "transaction " + DtsContext.getInstance().getCurrentXid() + " Global commit failed.");
      }
    } finally {
      DtsContext.getInstance().unbind();
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
    int transId = DtsXID.getTransactionId(DtsContext.getInstance().getCurrentXid());
    rollbackMessage.setTranId(transId);
    long start = 0;
    if (logger.isDebugEnabled())
      start = System.currentTimeMillis();
    GlobalRollbackResultMessage resultMessage = null;
    try {
      Exception ex = null;
      do {
        try {
          resultMessage = (GlobalRollbackResultMessage) dtsClient.invoke(rollbackMessage,
              RemoteConstant.RPC_INVOKE_TIMEOUT);
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
        throw new DtsException("transaction " + DtsContext.getInstance().getCurrentXid()
            + " Global rollback failed.server response is null");
      }
      if (ex != null) {
        throw new DtsException(ex,
            "transaction " + DtsContext.getInstance().getCurrentXid() + " Global rollback failed.");
      }
    } finally {
      DtsContext.getInstance().unbind();
      if (logger.isDebugEnabled()) {
        long end = System.currentTimeMillis();
        logger.debug("invoke global rollback message:" + rollbackMessage + " cost " + (end - start)
            + " ms.");
      }
    }
  }

}
