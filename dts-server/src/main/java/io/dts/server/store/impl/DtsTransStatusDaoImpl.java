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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import io.dts.server.handler.support.ClientMessageHandler;
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
  private final DelayQueue<DelayedItem<Long>> queue = new DelayQueue<DelayedItem<Long>>();
  private ClientMessageHandler handler;
  /**
   * 当前活动的所有事务分支
   */
  private static final ConcurrentHashMap<Long, BranchLog> activeTranBranchMap =
      new ConcurrentHashMap<Long, BranchLog>();

  @PostConstruct
  public void init() {
    Thread t = new Thread() {
      @Override
      public void run() {
        while (true) {
          DelayedItem<Long> delayedItem = queue.poll();
          if (delayedItem != null) {
            Long transId = delayedItem.getT();
            // if (handler != null) {
            // GlobalRollbackMessage rollback = new GlobalRollbackMessage();
            // rollback.setTranId(transId);
            // handler.processMessage(rollback);
            // }
          }
          try {
            Thread.sleep(300);
          } catch (Exception e) {
          }
        }
      }
    };
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void setClientMessageHandler(ClientMessageHandler handler) {
    this.handler = handler;
  }

  @Override
  public void saveGlobalLog(Long tranId, GlobalLog globalLog, long liveTime) {
    GlobalLog v2 = activeTranMap.put(tranId, globalLog);
    DelayedItem<Long> tmpItem = new DelayedItem<Long>(tranId, liveTime);
    if (v2 != null) {
      queue.remove(tmpItem);
    }
    queue.put(tmpItem);
  }

  @Override
  public GlobalLog removeGlobalLog(Long transId) {
    return activeTranMap.remove(transId);
  }

  @Override
  public void saveBranchLog(Long branchId, BranchLog branchLog) {
    activeTranBranchMap.put(branchId, branchLog);
  }

  @Override
  public BranchLog removeBranchLog(Long branchId) {
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

  private static class DelayedItem<T> implements Delayed {

    private T t;
    private long liveTime;
    private long removeTime;

    public DelayedItem(T t, long liveTime) {
      this.setT(t);
      this.liveTime = liveTime;
      this.removeTime =
          TimeUnit.NANOSECONDS.convert(liveTime, TimeUnit.NANOSECONDS) + System.nanoTime();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Delayed o) {
      if (o == null)
        return 1;
      if (o == this)
        return 0;
      if (o instanceof DelayedItem) {
        DelayedItem<T> tmpDelayedItem = (DelayedItem<T>) o;
        if (liveTime > tmpDelayedItem.liveTime) {
          return 1;
        } else if (liveTime == tmpDelayedItem.liveTime) {
          return 0;
        } else {
          return -1;
        }
      }
      long diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
      return diff > 0 ? 1 : diff == 0 ? 0 : -1;
    }

    @Override
    public long getDelay(TimeUnit unit) {
      return unit.convert(removeTime - System.nanoTime(), unit);
    }

    public T getT() {
      return t;
    }

    public void setT(T t) {
      this.t = t;
    }

    @Override
    public int hashCode() {
      return t.hashCode();
    }

    @Override
    public boolean equals(Object object) {
      if (object instanceof DelayedItem) {
        return object.hashCode() == hashCode() ? true : false;
      }
      return false;
    }

  }


}
