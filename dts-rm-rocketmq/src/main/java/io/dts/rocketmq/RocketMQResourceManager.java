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
package io.dts.rocketmq;

import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.api.DtsClientMessageSender;
import io.dts.common.context.DtsContext;
import io.dts.common.context.DtsXID;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.remoting.RemoteConstant;
import io.dts.resourcemanager.DataSourceResourceManager;
import io.dts.resourcemanager.ResourceManager;
import io.dts.resourcemanager.network.DefaultDtsResourcMessageSender;

/**
 * @author liushiming
 * @version RocketMQResourceManager.java, v 0.0.1 2017年11月16日 上午11:20:16 liushiming
 */
public class RocketMQResourceManager implements ResourceManager {

  private static final Logger logger = LoggerFactory.getLogger(DataSourceResourceManager.class);

  private static ResourceManager resourceManager = new RocketMQResourceManager();

  private final DtsClientMessageSender resourceMessageSender;

  private DefaultMQProducerImpl defaultMQProducerImpl;

  private RocketMQResourceManager() {
    DefaultDtsResourcMessageSender messageSender = DefaultDtsResourcMessageSender.getInstance();
    messageSender.registerResourceManager(this);
    this.resourceMessageSender = messageSender;
    messageSender.start();
  }

  public static ResourceManager newResourceManager() {
    return resourceManager;
  }


  public DefaultMQProducerImpl getDefaultMQProducerImpl() {
    return defaultMQProducerImpl;
  }

  public void setDefaultMQProducerImpl(DefaultMQProducerImpl defaultMQProducerImpl) {
    this.defaultMQProducerImpl = defaultMQProducerImpl;
  }

  public DtsClientMessageSender getResourceMessageSender() {
    return resourceMessageSender;
  }

  @Override
  public long register(String topic) throws DtsException {
    if (DtsContext.getInstance().inTxcTransaction()) {
      RegisterMessage registerMessage = new RegisterMessage();
      registerMessage.setClientInfo(topic);
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
  public String getRegisterKey() {
    throw new UnsupportedOperationException("unsupport method in rocketmq resourcemanager");
  }

  @Override
  public void branchCommit(String xid, long branchId, String resourceInfo) throws DtsException {
    SendResult sendResult = SendResult.decoderSendResultFromJson(resourceInfo);
    LocalTransactionState localTransactionState = LocalTransactionState.COMMIT_MESSAGE;
    try {
      defaultMQProducerImpl.endTransaction(sendResult, localTransactionState, null);
    } catch (Exception e) {
      logger.warn("local transaction execute " + localTransactionState
          + ",but end broker transaction failed", e);
    }
  }

  @Override
  public void branchRollback(String xid, long branchId, String resourceInfo) throws DtsException {
    SendResult sendResult = SendResult.decoderSendResultFromJson(resourceInfo);
    LocalTransactionState localTransactionState = LocalTransactionState.ROLLBACK_MESSAGE;
    try {
      defaultMQProducerImpl.endTransaction(sendResult, localTransactionState, null);
    } catch (Exception e) {
      logger.warn("local transaction execute " + localTransactionState
          + ",but end broker transaction failed", e);
    }
  }

}
