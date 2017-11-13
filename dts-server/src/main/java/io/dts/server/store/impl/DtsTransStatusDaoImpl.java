/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.dts.server.store.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;

import io.dts.server.store.DtsTransStatusDao;
import io.dts.server.struct.BranchLog;
import io.dts.server.struct.GlobalLog;

/**
 * @author liushiming
 * @version DtsTransStatusDaoImpl.java, v 0.0.1 2017年9月18日 下午4:14:49 liushiming
 */
@Repository
public class DtsTransStatusDaoImpl implements DtsTransStatusDao {
  /**
   * 当前活动的所有事务
   */
  private static final ConcurrentHashMap<Long, GlobalLog> activeTranMap =
      new ConcurrentHashMap<Long, GlobalLog>();
  /**
   * 当前活动的所有事务分支
   */
  private static final ConcurrentHashMap<Long, BranchLog> activeTranBranchMap =
      new ConcurrentHashMap<Long, BranchLog>();

  /**
   * 超时的事务列表
   */
  private static final List<Long> timeoutTranList =
      Collections.synchronizedList(Lists.newArrayList());

  /**
   * 对于可重试分支，没有必要建立多个事务，这些分支对应同一个global log即可
   */
  private GlobalLog retryGlobalLog = null;

  @Override
  public void insertGlobalLog(Long tranId, GlobalLog globalLog) {
    activeTranMap.put(tranId, globalLog);
  }

  @Override
  public void clearGlobalLog(Long transId) {
    activeTranMap.remove(transId);
  }

  @Override
  public void insertBranchLog(Long branchId, BranchLog branchLog) {
    activeTranBranchMap.put(branchId, branchLog);
  }

  @Override
  public BranchLog clearBranchLog(Long branchId) {
    return activeTranBranchMap.remove(branchId);
  }

  @Override
  public GlobalLog queryGlobalLog(Long transId) {
    return activeTranMap.get(transId);
  }

  @Override
  public BranchLog queryBranchLog(Long branchId) {
    return activeTranBranchMap.get(branchId);
  }

  @Override
  public List<BranchLog> queryBranchLogByTransId(long tranId) {
    List<BranchLog> branchLogs = new ArrayList<BranchLog>();
    Map<Long, BranchLog> branchMap = activeTranBranchMap;
    GlobalLog globalLog;
    globalLog = activeTranMap.get(tranId);
    if (globalLog == null)
      return branchLogs;
    for (long branchId : globalLog.getBranchIds()) {
      BranchLog branchLog;
      if ((branchLog = branchMap.get(branchId)) != null) {
        branchLogs.add(branchLog);
      }
    }
    return branchLogs;
  }

  @Override
  public boolean queryTimeOut(Long transId) {
    return timeoutTranList.contains(transId);
  }

  @Override
  public boolean removeTimeOut(Long transId) {
    timeoutTranList.remove(transId);
    return true;
  }

  @Override
  public GlobalLog getRetryGlobalLog() {
    return retryGlobalLog;
  }

  @Override
  public void setRetryGlobalLog(GlobalLog retryGlobalLog) {
    this.retryGlobalLog = retryGlobalLog;
  }

}
