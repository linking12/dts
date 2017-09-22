package io.dts.resourcemanager.at;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.common.CommitMode;
import io.dts.common.common.TxcXID;
import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.common.protocol.header.ReportStatusMessage;
import io.dts.common.protocol.header.ReportStatusResultMessage;
import io.dts.remoting.protocol.RemotingSysResponseCode;
import io.dts.resourcemanager.ResourceManager;

/**
 * Created by guoyubo on 2017/9/13.
 */
public class BaseResourceManager implements ResourceManager {

  private static final Logger logger = LoggerFactory.getLogger(BaseResourceManager.class);

  private DtsClientMessageSender clientMessageSender;

  public BaseResourceManager(final DtsClientMessageSender clientMessageSender) {
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
        RegisterResultMessage resultMessage =
            clientMessageSender.invoke(serverAddress, registerMessage, 3000l);
        if (resultMessage.getResult() != RemotingSysResponseCode.SUCCESS) {
          throw new DtsException(resultMessage.getResult(), "error");
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
  public void reportStatus(final long branchId, final boolean success, final String key,
      final String udata) throws DtsException {
    if (DtsContext.inTxcTransaction()) {
      ReportStatusMessage reportStatusMessage = new ReportStatusMessage();
      reportStatusMessage.setBranchId(branchId);
      reportStatusMessage.setKey(key);
      reportStatusMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));
      reportStatusMessage.setSuccess(success);
      reportStatusMessage.setUdata(udata);
      final String serverAddress = TxcXID.getServerAddress(DtsContext.getCurrentXid());
      ReportStatusResultMessage resultMessage =
          clientMessageSender.invoke(serverAddress, reportStatusMessage, 3000l);
    } else {
      throw new IllegalStateException("current thread is not bind to dts transaction.");
    }
  }



}
