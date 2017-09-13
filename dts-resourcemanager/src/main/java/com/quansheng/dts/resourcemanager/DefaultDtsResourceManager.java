package com.quansheng.dts.resourcemanager;

import com.quancheng.dts.RemotingSerializable;
import com.quancheng.dts.RequestCode;
import com.quancheng.dts.common.CommitMode;
import com.quancheng.dts.common.DtsContext;
import com.quancheng.dts.common.DtsXID;
import com.quancheng.dts.event.eventbus.EventBusFactory;
import com.quancheng.dts.event.message.BranchRegisterEvent;
import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.message.request.RegisterMessage;
import com.quancheng.dts.message.request.ReportStatusMessage;
import com.quancheng.dts.message.response.RegisterResultMessage;
import com.quancheng.dts.message.response.ReportStatusResultMessage;
import com.quancheng.dts.rpc.remoting.DtsClient;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;
import com.quansheng.dts.resourcemanager.attribute.TxcIsolation;
import com.quansheng.dts.resourcemanager.undo.UndoLogManager;

import java.sql.Connection;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by guoyubo on 2017/9/6.
 */
@Slf4j
public class DefaultDtsResourceManager implements DtsResourceManager {

  private int reportRetryTime = 5; // 分支汇报状态时，如果失败，则进行重试

  private long defaultTimeout = 3000;

  private DtsClient dtsClient;

  private UndoLogManager undoLogManager;

  public DefaultDtsResourceManager(final DtsClient dtsClient) {
    this.dtsClient = dtsClient;
  }

  public int getReportRetryTime() {
    return reportRetryTime;
  }

  public void setReportRetryTime(int reportRetryTime) {
    this.reportRetryTime = reportRetryTime;
  }

  public long getDefaultTimeout() {
    return defaultTimeout;
  }

  public void setDefaultTimeout(final long defaultTimeout) {
    this.defaultTimeout = defaultTimeout;
  }

  @Override
  public long register(final String key, final CommitMode commitMode) throws DtsException {
    final RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.BRANCH_REGISTER, null);
    RegisterMessage registerMessage = new RegisterMessage();
    registerMessage.setKey(key);
    registerMessage.setTranId(DtsXID.getTransactionId(DtsContext.getCurrentXid()));
    request.setBody(RemotingSerializable.encode(registerMessage));
    try {
      RegisterResultMessage registerResultMessage = dtsClient.invokeSync(request, defaultTimeout, RegisterResultMessage.class);
      System.out.println(registerResultMessage);
      DtsContext.bindBranch(key, registerResultMessage.getBranchId());
      return registerResultMessage.getBranchId();
    } catch (DtsException e) {
      log.error("register branch error", e);
    }
    return 0;
  }

  @Override
  public int insertUndoLog(final Connection conn, final String xid, final Long branchId, final String serverAddr,
      final int status)
      throws DtsException {
    return undoLogManager.insertUndoLog(conn, xid, branchId, serverAddr, status);
  }

  @Override
  public void reportStatus(final long branchId, final boolean success, final String key, final String udata)
      throws DtsException {
    int retry = getReportRetryTime();
    for (int i = 1;; i++) {
      try {
        reportStatus(branchId, success, key, udata, i);
        break;
      } catch (DtsException e) {
        if (i <= retry) {
          log.error("reportStatus branch:" + branchId + ", retry:" + i, e);
        } else {
          log.error("reportStatus branch:" + branchId, e);
          throw e;
        }
      }
    }

  }


  private void reportStatus(long branchId, boolean success, String key, String udata, int tryTime) throws DtsException {
    if (DtsContext.inTxcTransaction()) {
      final RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.REPORT_STATUS, null);

      ReportStatusMessage reportStatusMessage = new ReportStatusMessage();
      reportStatusMessage.setBranchId(branchId);
      reportStatusMessage.setSuccess(success);
      reportStatusMessage.setKey(key);
      reportStatusMessage.setTranId(DtsXID.getTransactionId(DtsContext.getCurrentXid()));
      reportStatusMessage.setUdata(udata);
      request.setBody(RemotingSerializable.encode(reportStatusMessage));
      try {
        ReportStatusResultMessage reportStatusResultMessage =
            dtsClient.invokeSync(request, defaultTimeout, ReportStatusResultMessage.class);
      } catch (Throwable th) {
        throw new DtsException(th);
      } finally {
      }
    } else {
      throw new IllegalStateException("current thread is not bind to txc transaction.");
    }
  }

  @Override
  public void reportUdata(final String xid, final long branchId, final String key, final String udata,
      final boolean delay) throws DtsException {

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

  @Override
  public TxcIsolation getIsolationLevel() {
    return null;
  }
}
