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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;

import io.dts.common.common.CommitMode;
import io.dts.common.common.TrxLockMode;
import io.dts.common.common.TxcXID;
import io.dts.common.common.context.ContextStep2;
import io.dts.common.common.exception.DtsException;
import io.dts.resourcemanager.helper.TxcTrxConfig;
import io.dts.resourcemanager.logmanager.DtsLogManager;
import io.dts.resourcemanager.struct.TxcBranchStatus;
import io.dts.resourcemanager.struct.TxcIsolation;

/**
 * @author liushiming
 * @version AtResourceManager.java, v 0.0.1 2017年10月16日 下午3:18:14 liushiming
 */
public class AtResourceManager extends BaseResourceManager {

  private static ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(1);

  private static Map<Long, ContextStep2> currentTaskCommitedAt = Maps.newConcurrentMap();

  private static Map<String, TxcBranchStatus> currentTaskMap = Maps.newConcurrentMap();
  /**
   * 隔离级别
   */
  private TxcIsolation trxLevel = TxcIsolation.READ_UNCOMMITED;
  /**
   * 事务配置
   */
  private TxcTrxConfig trxConfig = new TxcTrxConfig();

  public TxcTrxConfig getTrxConfig() {
    return trxConfig;
  }

  public void setTrxConfig(TxcTrxConfig trxConfig) {
    this.trxConfig = trxConfig;
  }

  public TxcIsolation getIsolationLevel() {
    switch (trxLevel) {
      case READ_COMMITED:
      case READ_UNCOMMITED:
        break;
      case READ_COMMITED_REDO:
        break;
      case repeatable:
        throw new DtsException("unsupported repeatable isolation.");
      case serializable:
        throw new DtsException("unsupported serializable isolation.");
      default:
        throw new DtsException("undefined TxcIsolation:" + trxLevel.value());
    }
    return trxLevel;
  }

  public void setTrxLevel(TxcIsolation trxLevel) {
    this.trxLevel = trxLevel;
  }

  public void init() {
    try {
      timerExecutor.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          List<ContextStep2> list = new ArrayList<ContextStep2>();
          Iterator<Entry<Long, ContextStep2>> it = currentTaskCommitedAt.entrySet().iterator();
          while (it.hasNext()) {
            Entry<Long, ContextStep2> entry = it.next();
            ContextStep2 context = entry.getValue();
            it.remove();
            list.add(context);
          }
          try {
            DtsLogManager.getInstance().branchCommit(list);
          } catch (SQLException e) {
            throw new DtsException(e);
          }
        }
      }, 10, 1000 * 5, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      throw new DtsException(e);
    }
  }

  @Override
  public void branchCommit(String xid, long branchId, String key, String udata, int commitMode,
      String retrySql) throws DtsException {
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
      switch (context.getCommitMode()) {
        case COMMIT_IN_PHASE1:
        case COMMIT_IN_PHASE2:
          currentTaskCommitedAt.put(context.getGlobalXid(), context);
          break;
        case COMMIT_RETRY_MODE:
          DtsLogManager.getInstance().branchCommit(Arrays.asList(context));
          break;
        default:
          break;
      }
    } catch (DtsException e) {
      throw e;
    } catch (SQLException e) {
      throw new DtsException(e);
    } finally {
      currentTaskMap.remove(branchName);
    }
  }

  @Override
  public void branchRollback(String xid, long branchId, String key, String udata, int commitMode,
      int isDelKey) throws DtsException {
    String branchName = TxcXID.getBranchName(xid, branchId);
    if (currentTaskMap.containsKey(branchName)) {
      throw new DtsException("Branch is working:" + currentTaskMap.get(branchName));
    }

    if (currentTaskMap.put(branchName, TxcBranchStatus.ROLLBACKING) != null) {
      throw new DtsException("Branch is working:" + currentTaskMap.get(branchName));
    }

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
    if (isDelKey == TrxLockMode.DELETE_TRX_LOCK.getValue()) {
      context.setLockMode(TrxLockMode.DELETE_TRX_LOCK);
    } else if (isDelKey == TrxLockMode.NOT_DELETE_TRX_LOCK.getValue()) {
      context.setLockMode(TrxLockMode.NOT_DELETE_TRX_LOCK);
    }
    context.setGlobalXid(TxcXID.getGlobalXID(xid, branchId));
    try {
      DtsLogManager.getInstance().branchRollback(context);
    } catch (DtsException e) {
      throw e;
    } catch (SQLException e) {
      throw new DtsException(e);
    } finally {
      currentTaskMap.remove(branchName);
    }
  }

  @Override
  public void reportUdata(String xid, long branchId, String key, String udata, boolean delay)
      throws DtsException {
    throw new UnsupportedOperationException("method not support");
  }

  @Override
  public void branchRollback(String xid, long branchId, String key, String udata, int commitMode)
      throws DtsException {
    throw new UnsupportedOperationException("method not support");
  }
}
