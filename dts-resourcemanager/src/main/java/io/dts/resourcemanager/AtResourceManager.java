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
package io.dts.resourcemanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.dts.common.common.context.ContextStep2;
import io.dts.common.common.exception.DtsException;
import io.dts.resourcemanager.api.IDtsLogManager;
import io.dts.resourcemanager.help.TxcTrxConfig;
import io.dts.resourcemanager.struct.TxcBranchStatus;
import io.dts.resourcemanager.struct.TxcIsolation;

/**
 * @author liushiming
 * @version AtResourceManager.java, v 0.0.1 2017年10月16日 下午3:18:14 liushiming
 */
public class AtResourceManager extends BaseResourceManager {
  private static Map<String, TxcBranchStatus> currentTaskMap =
      new ConcurrentHashMap<String, TxcBranchStatus>();

  private static ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(1);

  private static Map<Long, ContextStep2> currentTaskCommitedAt =
      new ConcurrentHashMap<Long, ContextStep2>();

  private IDtsLogManager txcSqlLogManager = null;
  /**
   * 隔离级别
   */
  private TxcIsolation trxLevel = TxcIsolation.READ_UNCOMMITED;
  /**
   * 事务配置
   */
  private TxcTrxConfig trxConfig = new TxcTrxConfig();

  @Override
  public void reportUdata(String xid, long branchId, String key, String udata, boolean delay)
      throws DtsException {
    // TODO Auto-generated method stub

  }

  @Override
  public void branchCommit(String xid, long branchId, String key, String udata, int commitMode,
      String retrySql) throws DtsException {
    // TODO Auto-generated method stub

  }

  @Override
  public void branchRollback(String xid, long branchId, String key, String udata, int commitMode)
      throws DtsException {
    // TODO Auto-generated method stub

  }

  @Override
  public void branchRollback(String xid, long branchId, String key, String udata, int commitMode,
      int isDelKey) throws DtsException {
    // TODO Auto-generated method stub

  }

}
