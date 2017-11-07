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
package io.dts.common.api;

import io.dts.common.exception.DtsException;
import io.dts.common.protocol.RequestCode;
import io.dts.common.protocol.RequestMessage;
import io.dts.common.protocol.ResponseMessage;
import io.dts.common.protocol.heatbeat.HeartbeatRequestHeader;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.exception.RemotingCommandException;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;
import io.dts.remoting.protocol.RemotingSysResponseCode;

/**
 * @author liushiming
 * @version AbstractMessageSender.java, v 0.0.1 2017年9月22日 下午4:48:30 liushiming
 */
public interface BaseMessageSender {
  default RemotingCommand buildRequest(RequestMessage dtsMessage) throws DtsException {
    RemotingCommand request = null;
    if (dtsMessage instanceof CommandCustomHeader) {
      if (dtsMessage instanceof HeartbeatRequestHeader) {
        request = RemotingCommand.createRequestCommand(RequestCode.HEART_BEAT,
            (CommandCustomHeader) dtsMessage);
      } else {
        request = RemotingCommand.createRequestCommand(RequestCode.HEADER_REQUEST,
            (CommandCustomHeader) dtsMessage);
      }
    } else if (dtsMessage instanceof RemotingSerializable) {
      request = RemotingCommand.createRequestCommand(RequestCode.BODY_REQUEST, null);
      request.setBody(RemotingSerializable.encode1(dtsMessage));
    } else {
      throw new DtsException("request must implements CommandCustomHeader or RemotingSerializable");
    }
    return request;
  }

  @SuppressWarnings("unchecked")
  default <T> T buildResponse(RemotingCommand response) throws RemotingCommandException {
    if (response.getCode() == RemotingSysResponseCode.SUCCESS) {
      if (!response.getExtFields().isEmpty()) {
        return (T) response.decodeCommandCustomHeader(CommandCustomHeader.class);
      } else if (response.getBody() != null) {
        return (T) RemotingSerializable.decode(response.getBody(), ResponseMessage.class);
      }
    } else {
      throw new DtsException(response.getRemark());
    }
    return null;
  }
}
