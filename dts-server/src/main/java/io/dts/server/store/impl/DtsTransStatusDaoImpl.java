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
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.dts.server.model.BranchLog;
import io.dts.server.model.GlobalLog;
import io.dts.server.store.DtsTransStatusDao;

/**
 * @author liushiming
 * @version DtsTransStatusDaoImpl.java, v 0.0.1 2017年9月18日 下午4:14:49 liushiming
 */
public class DtsTransStatusDaoImpl implements DtsTransStatusDao {
  /**
   * 当前活动的所有事务
   */
  private static final Map<Long, GlobalLog> activeTranMap = Maps.newConcurrentMap();
  /**
   * 当前活动的所有事务分支
   */
  private static final Map<Long, BranchLog> activeTranBranchMap = Maps.newConcurrentMap();
  /**
   * 保存已经发送BranchCommitMessage消息，但是还没收到响应或者失败的分支
   */
  private static final Map<Long, Integer> committingMap = Maps.newConcurrentMap();

  /**
   * 保存已经发送BranchRollbackMessage消息，但是还没收到响应或者失败的分支
   */
  private static final Map<Long, Integer> rollbackingMap = Maps.newConcurrentMap();

  /**
   * 超时的事务列表
   */
  private static final List<Long> timeoutTranList =
      Collections.synchronizedList(Lists.newArrayList());

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
  public void clearBranchLog(Long branchId) {
    activeTranBranchMap.remove(branchId);
  }

  @Override
  public void insertCommitedBranchLog(Long branchId, Integer commitResultCode) {
    committingMap.put(branchId, commitResultCode);
  }

  @Override
  public void insertRollbackBranchLog(Long branchId, Integer rollbackingResultCode) {
    rollbackingMap.put(branchId, rollbackingResultCode);
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
  public List<BranchLog> queryBranchLogByTransId(long tranId, boolean sort, boolean reverse,
      boolean fromBkup) {
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
    if (sort) {
      if (reverse) {
        Collections.sort(branchLogs, new Comparator<BranchLog>() {
          @Override
          public int compare(BranchLog o1, BranchLog o2) {
            return (int) (o2.getBranchId() - o1.getBranchId());
          }
        });
      } else {
        Collections.sort(branchLogs, new Comparator<BranchLog>() {
          @Override
          public int compare(BranchLog o1, BranchLog o2) {
            return (int) (o1.getBranchId() - o2.getBranchId());
          }
        });
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


}
