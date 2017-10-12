package io.dts.resourcemanager.handler;

import io.dts.common.common.exception.DtsException;

/**
 * Created by guoyubo on 2017/9/15.
 */
public interface IBranchTransProcessHandler {

  /**
   * 分支提交
   *
   * @param xid
   * @param branchId
   * @param key
   * @param udata
   * @param commitMode
   * @throws DtsException
   */
  public void branchCommit(String xid, long branchId, String key, String udata, int commitMode, String retrySql) throws DtsException;

  /**
   * 分支回滚
   *
   * @param xid
   * @param branchId
   * @param key
   * @param udata
   * @param commitMode
   * @throws DtsException
   */
  public void branchRollback(String xid, long branchId, String key, String udata, int commitMode) throws DtsException;

  public void branchRollback(String xid, long branchId, String key, String udata, int commitMode, int isDelKey) throws DtsException;

}
