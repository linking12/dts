package io.dts.resourcemanager.core;

import io.dts.common.common.CommitMode;
import io.dts.common.exception.DtsException;

/**
 * Created by guoyubo on 2017/9/13.
 */
public interface ResourceManager {

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
   * @param udata 用户数据，可用来透传一些数据帮助回滚
   * @throws DtsException
   */
  public void reportStatus(long branchId, boolean success, String key, String udata) throws DtsException;



}
