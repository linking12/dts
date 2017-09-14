package io.dts.resourcemanager.at;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.CommitMode;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.DtsMessage;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSysResponseCode;
import io.dts.resourcemanager.ResourceManager;

/**
 * Created by guoyubo on 2017/9/13.
 */
public class ATResourceManager implements ResourceManager {

  private static final Logger logger = LoggerFactory.getLogger(ATResourceManager.class);

  private DtsClientMessageSender clientMessageSender;

  public ATResourceManager(final DtsClientMessageSender clientMessageSender) {
    this.clientMessageSender = clientMessageSender;
  }

  @Override
  public long register(final String key, final CommitMode commitMode) throws DtsException {
    if (DtsContext.inTxcTransaction()) {
      RegisterMessage registerMessage = new RegisterMessage();
      registerMessage.setKey(key);
      registerMessage.setCommitMode((byte) commitMode.getValue());
      registerMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));

      try {

        final String serverAddress = TxcXID.getServerAddress(DtsContext.getCurrentXid());
        RegisterResultMessage
            resultMessage = clientMessageSender.invoke(serverAddress, RequestCode.HEADER_REQUEST, registerMessage, 3000l);

        if (resultMessage.getResult() != RemotingSysResponseCode.SUCCESS) {
          throw new DtsException(resultMessage.getResult(), resultMessage.getMsg());
        } else {
          return resultMessage.getBranchId();
        }
      } catch (Throwable th) {
        logger.error("", "invoke msg failed. " + registerMessage);
        throw new DtsException(th);
      } finally {

      }
    } else {
      throw new IllegalStateException("current thread is not bind to dts transaction.");
    }
  }

  @Override
  public void reportStatus(final long branchId, final boolean success, final String key, final String udata)
      throws DtsException {

  }

  @Override
  public void branchCommit(final String xid, final long branchId, final String key, final String udata,
      final byte commitMode, final String retrySql)
      throws DtsException {

  }

  @Override
  public void branchRollback(final String xid, final long branchId, final String key, final String udata,
      final byte commitMode) throws DtsException {

  }

  @Override
  public void branchRollback(final String xid, final long branchId, final String key, final String udata,
      final byte commitMode, final int isDelKey)
      throws DtsException {
  }

}
