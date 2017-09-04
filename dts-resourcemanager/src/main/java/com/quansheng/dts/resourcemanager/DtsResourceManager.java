package com.quansheng.dts.resourcemanager;

import com.quancheng.dts.common.CommitMode;
import com.quancheng.dts.exception.DtsException;

/**
 * Created by guoyubo on 2017/9/4.
 */
public interface DtsResourceManager {

  /**
   * 分支注册
   *
   * @param key
   * @param commitMode
   * @return
   * @throws DtsException
   */
  public long register(String key, CommitMode commitMode) throws DtsException;

  /**
   * 分支状态上报
   *
   * @param branchId
   * @param success
   * @param key
   * @param udata
   *            用户数据，可用来透传一些数据帮助回滚
   * @throws DtsException
   */
  public void reportStatus(long branchId, boolean success, String key, String udata) throws DtsException;

  /**
   * 分支用户数据上报，可辅助帮助提交或回滚 仅对MT模式有效
   *
   * @param xid
   * @param branchId
   * @param key
   * @param udata
   *            用户数据
   * @param delay
   *            是否延时上报；如果为false，立即上报到TXC server； 如果为true，分支上报状态时一起上报到TXC
   *            Server，可以减少一次RPC
   * @throws DtsException
   * @since 1.1.0
   */
  public void reportUdata(String xid, long branchId, String key, String udata, boolean delay) throws DtsException;

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
  public void branchCommit(String xid, long branchId, String key, String udata, byte commitMode, String retrySql) throws
      DtsException;

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
