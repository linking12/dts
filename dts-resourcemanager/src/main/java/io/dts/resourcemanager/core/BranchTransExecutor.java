package io.dts.resourcemanager.core;

import io.dts.common.exception.DtsException;

/**
 * Created by guoyubo on 2017/9/15.
 */
public interface BranchTransExecutor {

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
  public void branchCommit(String xid, long branchId, String key, String udata, byte commitMode, String retrySql) throws DtsException;

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
  public void branchRollback(String xid, long branchId, String key, String udata, byte commitMode) throws DtsException;

  public void branchRollback(String xid, long branchId, String key, String udata, byte commitMode, int isDelKey) throws DtsException;

}
