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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.context.DtsContext;
import io.dts.common.context.DtsXID;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.remoting.RemoteConstant;
import io.dts.resourcemanager.logmanager.DtsLogManager;
import io.dts.resourcemanager.network.DefaultDtsResourcMessageSender;
import io.dts.resourcemanager.struct.ContextStep2;

/**
 * @author liushiming
 * @version BaseResourceManager.java, v 0.0.1 2017年10月13日 下午2:28:51 liushiming
 */
public class DataSourceResourceManager implements ResourceManager {
  private static final Logger logger = LoggerFactory.getLogger(DataSourceResourceManager.class);

  private static ResourceManager resourceManager = new DataSourceResourceManager();

  private final DtsClientMessageSender resourceMessageSender;

  private volatile String dbName;

  private DataSourceResourceManager() {
    DefaultDtsResourcMessageSender messageSender = DefaultDtsResourcMessageSender.getInstance();
    messageSender.registerResourceManager(this);
    this.resourceMessageSender = messageSender;
    messageSender.start();
  }

  public static ResourceManager newResourceManager() {
    return resourceManager;
  }

  @Override
  public String getRegisterKey() {
    return this.dbName;
  }

  @Override
  public long register(String dbName) throws DtsException {
    this.dbName = dbName;
    if (DtsContext.getInstance().inTxcTransaction()) {
      RegisterMessage registerMessage = new RegisterMessage();
      registerMessage.setDbName(dbName);
      registerMessage.setTranId(DtsXID.getTransactionId(DtsContext.getInstance().getCurrentXid()));
      try {
        RegisterResultMessage resultMessage = (RegisterResultMessage) resourceMessageSender
            .invoke(registerMessage, RemoteConstant.RPC_INVOKE_TIMEOUT);
        if (logger.isDebugEnabled())
          logger.debug(registerMessage.toString());
        if (resultMessage == null) {
          throw new DtsException("register resourcemanager failed,");
        } else {
          return resultMessage.getBranchId();
        }
      } catch (Throwable th) {
        logger.error("invoke msg failed. " + registerMessage, th);
        throw new DtsException(th);
      }
    } else {
      throw new IllegalStateException("current thread is not bind to txc transaction.");
    }
  }

  @Override
  public void branchCommit(String xid, long branchId, String key, String udata)
      throws DtsException {
    try {
      ContextStep2 context = new ContextStep2();
      context.setXid(xid);
      context.setBranchId(branchId);
      context.setDbname(key);
      context.setUdata(udata);
      context.setGlobalXid(DtsXID.getGlobalXID(xid, branchId));
      DtsLogManager.getInstance().branchCommit(context);
    } catch (DtsException e) {
      throw e;
    } catch (SQLException e) {
      throw new DtsException(e);
    }
  }

  @Override
  public void branchRollback(String xid, long branchId, String key, String udata)
      throws DtsException {
    ContextStep2 context = new ContextStep2();
    context.setXid(xid);
    context.setBranchId(branchId);
    context.setDbname(key);
    context.setUdata(udata);
    context.setGlobalXid(DtsXID.getGlobalXID(xid, branchId));
    try {
      DtsLogManager.getInstance().branchRollback(context);
    } catch (DtsException e) {
      throw e;
    } catch (SQLException e) {
      throw new DtsException(e);
    }
  }

  protected <T> T invoke(RequestMessage msg) throws DtsException {
    return resourceMessageSender.invoke(msg, RemoteConstant.RPC_INVOKE_TIMEOUT);
  }


}
