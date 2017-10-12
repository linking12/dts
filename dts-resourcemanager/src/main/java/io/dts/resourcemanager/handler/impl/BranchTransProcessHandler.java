package io.dts.resourcemanager.handler.impl;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.dts.common.common.CommitMode;
import io.dts.common.common.TxcXID;
import io.dts.common.context.ContextStep2;
import io.dts.common.exception.DtsException;
import io.dts.resourcemanager.handler.IBranchTransProcessHandler;
import io.dts.resourcemanager.core.impl.DtsLogManager;

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
    String branchName = TxcXID.getBranchName(xid, branchId);
    if (currentTaskMap.containsKey(branchName)) {
      throw new DtsException("Branch is working:" + currentTaskMap.get(branchName));
    }

    if (currentTaskMap.put(branchName, TxcBranchStatus.COMMITING) != null) {
      throw new DtsException("Branch is working:" + currentTaskMap.get(branchName));
    }

    try {
      ContextStep2 context = new ContextStep2();
      context.setXid(xid);
      context.setBranchId(branchId);
      context.setDbname(key);
      context.setUdata(udata);
      if (commitMode == CommitMode.COMMIT_IN_PHASE1.getValue()) {
        context.setCommitMode(CommitMode.COMMIT_IN_PHASE1);
      } else if (commitMode == CommitMode.COMMIT_IN_PHASE2.getValue()) {
        context.setCommitMode(CommitMode.COMMIT_IN_PHASE2);
      } else if (commitMode == CommitMode.COMMIT_RETRY_MODE.getValue()) {
        context.setCommitMode(CommitMode.COMMIT_RETRY_MODE);
      }
      context.setRetrySql(retrySql);
      context.setGlobalXid(TxcXID.getGlobalXID(xid, branchId));

      txcLogManager.branchCommit(Arrays.asList(context));

    } catch (DtsException e) {
      throw e;
    } catch (SQLException e) {
      throw new DtsException(e);
    } finally {
      currentTaskMap.remove(branchName);
    }
  }

  @Override
  public void branchRollback(final String xid, final long branchId, final String key, final String udata,
      final int commitMode) throws DtsException {

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
