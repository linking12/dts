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
package io.dts.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.dts.common.api.DtsServerMessageSender;
import io.dts.common.exception.DtsException;
import io.dts.common.protocol.DtsMessage;
import io.dts.common.protocol.RequestCode;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.RemotingServer;
import io.dts.remoting.exception.RemotingCommandException;
import io.dts.remoting.exception.RemotingSendRequestException;
import io.dts.remoting.exception.RemotingTimeoutException;
import io.dts.remoting.exception.RemotingTooMuchRequestException;
import io.dts.remoting.protocol.RemotingCommand;
import io.dts.remoting.protocol.RemotingSerializable;
import io.dts.remoting.protocol.RemotingSysResponseCode;
import io.dts.server.remoting.channel.ChannelRepository;
import io.netty.channel.Channel;

/**
 * @author liushiming
 * @version DefaultDtsMessageSenderImpl.java, v 0.0.1 2017年9月20日 下午2:47:01 liushiming
 */
@Component
public class DefaultDtsServerMessageSender implements DtsServerMessageSender {


  @Autowired
  private ChannelRepository channelRepository;

  private RemotingServer remoteServer;

  public RemotingServer getRemoteServer() {
    return remoteServer;
  }

  public void setRemoteServer(RemotingServer remoteServer) {
    this.remoteServer = remoteServer;
  }

  private RemotingCommand buildRequest(DtsMessage dtsMessage) throws DtsException {
    RemotingCommand request = null;
    if (dtsMessage instanceof CommandCustomHeader) {
      request = RemotingCommand.createRequestCommand(RequestCode.HEADER_REQUEST,
          (CommandCustomHeader) dtsMessage);
    } else if (dtsMessage instanceof RemotingSerializable) {
      request = RemotingCommand.createRequestCommand(RequestCode.BODY_REQUEST, null);
      request.setBody(RemotingSerializable.encode(dtsMessage));
    }
    return request;
  }

  @SuppressWarnings("unchecked")
  private <T> T buildResponse(RemotingCommand response) throws RemotingCommandException {
    if (response.getCode() == RemotingSysResponseCode.SUCCESS) {
      if (response.getExtFields().isEmpty()) {
        return (T) response.decodeCommandCustomHeader(CommandCustomHeader.class);
      } else if (response.getBody() != null) {
        return (T) RemotingSerializable.decode(response.getBody(), DtsMessage.class);
      }
    } else {
      throw new DtsException(response.getRemark());
    }
    return null;
  }


  @Override
  public <T> T invokeSync(String clientAddress, DtsMessage msg, long timeout) throws DtsException {
    Channel channel = channelRepository.getChannelByAddress(clientAddress);
    if (channel != null) {
      RemotingCommand request = this.buildRequest(msg);
      try {
        RemotingCommand response = remoteServer.invokeSync(channel, request, timeout);
        return this.buildResponse(response);
      } catch (RemotingSendRequestException | RemotingTimeoutException | InterruptedException
          | RemotingCommandException e) {
        throw new DtsException(e);
      }
    }
    return null;
  }

  @Override
  public void invokeAsync(String clientAddress, DtsMessage msg, long timeout) throws DtsException {
    Channel channel = channelRepository.getChannelByAddress(clientAddress);
    if (channel != null) {
      RemotingCommand request = this.buildRequest(msg);
      try {
        remoteServer.invokeAsync(channel, request, timeout, null);
      } catch (RemotingSendRequestException | RemotingTimeoutException | InterruptedException
          | RemotingTooMuchRequestException e) {
        throw new DtsException(e);
      }
    }
  }

}
