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
import com.quancheng.dts.message.response.RegisterResultMessage;
import com.quancheng.dts.rpc.remoting.DtsClient;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by guoyubo on 2017/9/6.
 */
@Slf4j
public class DefaultDtsResourceManager implements DtsResourceManager {

  private DtsClient dtsClient;

  public DefaultDtsResourceManager(final DtsClient dtsClient) {
    this.dtsClient = dtsClient;
  }

  @Override
  public long register(final String key, final CommitMode commitMode) throws DtsException {
    final RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.BRANCH_REGISTER, null);
    RegisterMessage registerMessage = new RegisterMessage();
    registerMessage.setKey(key);
    registerMessage.setTranId(DtsXID.getTransactionId(DtsContext.getCurrentXid()));
    request.setBody(RemotingSerializable.encode(registerMessage));
    try {
      RegisterResultMessage registerResultMessage = dtsClient.invokeSync(request, null, RegisterResultMessage.class);
      System.out.println(registerResultMessage);
      DtsContext.bindBranch(key, registerResultMessage.getBranchId());
      return registerResultMessage.getBranchId();
    } catch (DtsException e) {
      log.error("register branch error", e);
    }
    return 0;
  }

  @Override
  public void reportStatus(final long branchId, final boolean success, final String key, final String udata)
      throws DtsException {

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
}
