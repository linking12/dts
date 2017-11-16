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

import org.apache.rocketmq.client.Validators;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.remoting.exception.RemotingException;

import io.dts.common.context.DtsContext;
import io.dts.common.exception.DtsException;
import io.dts.resourcemanager.ResourceManager;

/**
 * @author liushiming
 * @version DtsRocketMqProducer.java, v 0.0.1 2017年11月16日 上午10:29:20 liushiming
 */
public class DtsRocketMQProducer extends DefaultMQProducer {

  private final ResourceManager resourceManager;

  public DtsRocketMQProducer(final String producerGroup) {
    super(producerGroup);
    resourceManager = RocketMQResourceManager.newResourceManager();
  }

  @Override
  public SendResult send(Message msg)
      throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
    try {
      Validators.checkMessage(msg, this);
    } catch (Exception e) {
      throw new DtsException(e, "message validate error");
    }
    if (DtsContext.getInstance().inTxcTransaction()) {
      SendResult sendResult = null;
      try {
        MessageAccessor.putProperty(msg, MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");
        MessageAccessor.putProperty(msg, MessageConst.PROPERTY_PRODUCER_GROUP, getProducerGroup());
        sendResult = defaultMQProducerImpl.send(msg);
      } catch (Exception e) {
        throw new DtsException(e, "send message Exception");
      }
      SendStatus ss = sendResult.getSendStatus();
      if (ss == SendStatus.SEND_OK) {
        if (sendResult.getTransactionId() != null) {
          MessageAccessor.putProperty(msg, "__transactionId__", sendResult.getTransactionId());
        }
      }
      String sendResultKey = SendResult.encoderSendResultToJson(sendResult);
      resourceManager.register(sendResultKey);
      this.setDefaultMQProducerImpl();
      return sendResult;
    } else {
      return this.defaultMQProducerImpl.send(msg);
    }
  }

  private void setDefaultMQProducerImpl() {
    RocketMQResourceManager resourceManager_ = (RocketMQResourceManager) resourceManager;
    if (resourceManager_.getDefaultMQProducerImpl() == null)
      resourceManager_.setDefaultMQProducerImpl(defaultMQProducerImpl);
  }

}
