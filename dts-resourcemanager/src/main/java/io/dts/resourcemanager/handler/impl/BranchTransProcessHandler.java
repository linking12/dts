package io.dts.resourcemanager.handler.impl;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.dts.common.common.CommitMode;
import io.dts.common.common.TxcXID;
import io.dts.common.common.context.ContextStep2;
import io.dts.common.common.exception.DtsException;
import io.dts.resourcemanager.executor.DtsLogManager;
import io.dts.resourcemanager.handler.IBranchTransProcessHandler;

/**
 * Created by guoyubo on 2017/9/27.
 */
public class BranchTransProcessHandler implements IBranchTransProcessHandler {

  private static Map<String, TxcBranchStatus> currentTaskMap = new ConcurrentHashMap<String, TxcBranchStatus>();

  private DtsLogManager txcLogManager;

  public BranchTransProcessHandler(final DtsLogManager txcLogManager) {
    this.txcLogManager = txcLogManager;
  }

  @Override
  public void branchCommit(final String xid, final long branchId, final String key, final String udata,
      final int commitMode, final String retrySql)
      throws DtsException {
    String branchName = checkBranch(xid, branchId);

    try {
      ContextStep2 context = buildContextStep2(xid, branchId, key, udata, commitMode, retrySql);

      txcLogManager.branchCommit(Arrays.asList(context));

    } catch (DtsException e) {
      throw e;
    } catch (SQLException e) {
      throw new DtsException(e);
    } finally {
      currentTaskMap.remove(branchName);
    }
  }

  private ContextStep2 buildContextStep2(final String xid, final long branchId, final String key, final String udata,
      final int commitMode, final String retrySql) {
    ContextStep2 context = new ContextStep2();
    context.setXid(xid);
    context.setBranchId(branchId);
    context.setDbname(key);
    context.setUdata(udata);
    context.setCommitMode(CommitMode.to(commitMode));
    context.setRetrySql(retrySql);
    context.setGlobalXid(TxcXID.getGlobalXID(xid, branchId));
    return context;
  }

  @Override
  public void branchRollback(final String xid, final long branchId, final String key, final String udata,
      final int commitMode) throws DtsException {
    String branchName = checkBranch(xid, branchId);

    try {
      ContextStep2 context = buildContextStep2(xid, branchId, key, udata, commitMode, null);

      txcLogManager.branchRollback(context);

    } catch (DtsException e) {
      throw e;
    } catch (SQLException e) {
      throw new DtsException(e);
    } finally {
      currentTaskMap.remove(branchName);
    }
  }

  private String checkBranch(final String xid, final long branchId) {
    String branchName = TxcXID.getBranchName(xid, branchId);
    if (currentTaskMap.containsKey(branchName)) {
      throw new DtsException("Branch is working:" + currentTaskMap.get(branchName));
    }

    if (currentTaskMap.put(branchName, TxcBranchStatus.COMMITING) != null) {
      throw new DtsException("Branch is working:" + currentTaskMap.get(branchName));
    }
    return branchName;
  }

  @Override
  public void branchRollback(final String xid, final long branchId, final String key, final String udata,
      final int commitMode, final int isDelKey)
      throws DtsException {

  }


  enum TxcBranchStatus {
    COMMITING(1), ROLLBACKING(2);

    private TxcBranchStatus(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    private int value;
  }

}
