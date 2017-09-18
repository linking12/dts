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
package io.dts.server.store;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.dts.server.model.BranchLog;
import io.dts.server.model.GlobalLog;

/**
 * @author liushiming
 * @version DtsTranStateDao.java, v 0.0.1 2017年9月18日 下午4:01:20 liushiming
 */
public interface DtsTransStatusDao {
  /**
   * 当前活动的所有事务
   */
  static final Map<Long, GlobalLog> activeTranMap = Maps.newConcurrentMap();
  /**
   * 当前活动的所有事务分支
   */
  static final Map<Long, BranchLog> activeTranBranchMap = Maps.newConcurrentMap();
  /**
   * 保存已经发送BranchCommitMessage消息，但是还没收到响应或者失败的分支
   */
  static final Map<Long, Integer> committingMap = Maps.newConcurrentMap();

  /**
   * 保存已经发送BranchRollbackMessage消息，但是还没收到响应或者失败的分支
   */
  static final Map<Long, Integer> rollbackingMap = Maps.newConcurrentMap();

  /**
   * 超时的事务列表
   */
  static final List<Long> timeoutTranList = Collections.synchronizedList(Lists.newArrayList());

  public void insertGlobalLog(Long tranId, GlobalLog globalLog);

  public void insertBranchLog(Long branchId, BranchLog branchLog);

  public void insertCommitedBranchLog(Long branchId, Integer commitResultCode);

  public void insertRollbackBranchLog(Long branchId, Integer rollbackingResultCode);

  public List<BranchLog> queryBranchLogByTransId(Long transId);

}
