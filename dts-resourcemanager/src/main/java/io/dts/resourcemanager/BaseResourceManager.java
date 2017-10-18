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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.common.CommitMode;
import io.dts.common.common.TxcConstants;
import io.dts.common.common.TxcXID;
import io.dts.common.common.context.DtsContext;
import io.dts.common.common.exception.DtsException;
import io.dts.common.protocol.RequestMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.common.protocol.header.ReportStatusMessage;
import io.dts.common.rpc.DtsClientMessageSender;
import io.dts.resourcemanager.network.DefaultDtsResourcMessageSender;

/**
 * @author liushiming
 * @version BaseResourceManager.java, v 0.0.1 2017年10月13日 下午2:28:51 liushiming
 */
public abstract class BaseResourceManager implements ResourceManager {
  private static final Logger logger = LoggerFactory.getLogger(BaseResourceManager.class);

  static private HashMap<String, ResourceManager> m_registry =
      new HashMap<String, ResourceManager>();

  private final DtsClientMessageSender resourceMessageSender;

  protected BaseResourceManager() {
    DefaultDtsResourcMessageSender messageSender = DefaultDtsResourcMessageSender.getInstance();
    messageSender.registerResourceManager(this);
    this.resourceMessageSender = messageSender;
    messageSender.start();
  }

  static public ResourceManager getInstance(String name) {
    if (name == null) {
      throw new DtsException("null ResourceManager class name");
    }

    if (m_registry.get(name) == null) {
      try {
        m_registry.put(name, (ResourceManager) Class.forName(name).newInstance());
      } catch (Exception e) {
        throw new DtsException("Error happened:" + name);
      }
    }
    return (ResourceManager) (m_registry.get(name));
  }

  private int reportRetryTime = 5; // 分支汇报状态时，如果失败，则进行重试

  public int getReportRetryTime() {
    return reportRetryTime;
  }

  public void setReportRetryTime(int reportRetryTime) {
    this.reportRetryTime = reportRetryTime;
  }


  @Override
  public long register(String key, CommitMode commitMode) throws DtsException {
    if (DtsContext.inTxcTransaction()) {
      RegisterMessage registerMessage = new RegisterMessage();
      registerMessage.setKey(key);
      registerMessage.setCommitMode(commitMode.getValue());
      registerMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));
      try {
        RegisterResultMessage resultMessage = (RegisterResultMessage) resourceMessageSender
            .invoke(registerMessage, TxcConstants.RPC_INVOKE_TIMEOUT);
        if (logger.isDebugEnabled())
          logger.debug(registerMessage.toString());
        if (resultMessage == null) {
          throw new DtsException("register resourcemanager failed,");
        } else {
          return resultMessage.getBranchId();
        }
      } catch (Throwable th) {
        logger.error("", "invoke msg failed. " + registerMessage);
        throw new DtsException(th);
      } finally {
      }
    } else {
      throw new IllegalStateException("current thread is not bind to txc transaction.");
    }
  }

  @Override
  public void reportStatus(long branchId, boolean success, String key, String udata)
      throws DtsException {
    int retry = getReportRetryTime();
    for (int i = 1;; i++) {
      try {
        reportStatus(branchId, success, key, udata, i);
        break;
      } catch (DtsException e) {
        if (i <= retry) {
          logger.error("", "reportStatus branch:" + branchId + ", retry:" + i, e);
        } else {
          logger.error("", "reportStatus branch:" + branchId, e);
          throw e;
        }
      }
    }
  }

  private void reportStatus(long branchId, boolean success, String key, String udata, int tryTime)
      throws DtsException {
    if (DtsContext.inTxcTransaction()) {
      ReportStatusMessage reportStatusMessage = new ReportStatusMessage();
      reportStatusMessage.setBranchId(branchId);
      reportStatusMessage.setSuccess(success);
      reportStatusMessage.setKey(key);
      reportStatusMessage.setTranId(TxcXID.getTransactionId(DtsContext.getCurrentXid()));
      reportStatusMessage.setUdata(udata);

      try {
        if (logger.isDebugEnabled())
          logger.debug(reportStatusMessage.toString());
        resourceMessageSender.invoke(reportStatusMessage, -1);
      } catch (Throwable th) {
        throw new DtsException(th);
      } finally {
      }
    } else {
      throw new IllegalStateException("current thread is not bind to txc transaction.");
    }
  }

  protected <T> T invoke(RequestMessage msg) throws DtsException {
    return resourceMessageSender.invoke(msg, TxcConstants.RPC_INVOKE_TIMEOUT);
  }
}
