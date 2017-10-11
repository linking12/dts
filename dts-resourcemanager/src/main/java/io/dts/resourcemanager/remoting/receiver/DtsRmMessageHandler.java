package io.dts.resourcemanager.remoting.receiver;

import io.dts.common.protocol.ResultCode;
import io.dts.common.protocol.header.BranchCommitMessage;
import io.dts.common.protocol.header.BranchCommitResultMessage;
import io.dts.common.protocol.header.BranchRollBackMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.resourcemanager.handler.IBranchTransProcessHandler;


public class DtsRmMessageHandler {

  private IBranchTransProcessHandler branchTransProcessHandler;

  public DtsRmMessageHandler(final IBranchTransProcessHandler branchTransProcessHandler) {
    this.branchTransProcessHandler = branchTransProcessHandler;
  }

  public void handleMessage(final String serverAddressIp, final BranchCommitMessage commitMessage,
      final BranchCommitResultMessage resultMessage) {
    Long branchId = commitMessage.getBranchId();
    Long tranId = commitMessage.getTranId();
    String servAddr = commitMessage.getServerAddr();
    String dbName = commitMessage.getDbName();
    String udata = commitMessage.getUdata();
    int commitMode = commitMessage.getCommitMode();
    String retrySql = commitMessage.getRetrySql();

    resultMessage.setBranchId(branchId);
    resultMessage.setTranId(tranId);
    try {
      branchTransProcessHandler.branchCommit(servAddr + ":" + tranId, branchId, dbName, udata, commitMode, retrySql);
      resultMessage.setResult(ResultCode.OK.getValue());
    } catch (Exception e) {
      resultMessage.setResult(ResultCode.SYSTEMERROR.getValue());
    }
  }

  public void handleMessage(final String serverAddressIP, final BranchRollBackMessage message,
      final BranchRollbackResultMessage resultMessage) {

  }



}
