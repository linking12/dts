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
package io.dts.common.protocol;

import java.nio.ByteBuffer;

import io.dts.common.protocol.header.BeginMessage;
import io.dts.remoting.protocol.RemotingCommand;

/**
 * @author liushiming
 * @version TestMain.java, v 0.0.1 2017年9月6日 下午4:55:13 liushiming
 */
public class TestMain {

  public static void main(String[] args) {
    BeginMessage message = new BeginMessage();
//    RemotingCommand remotingCommand = RemotingCommand.createRequestCommand(200, message);
//    ByteBuffer requestCommand = remotingCommand.encode();
//    RemotingCommand serverCommand = RemotingCommand.decode(requestCommand);
//    System.out.println(serverCommand);
    System.out.println(message.getClass().getName());
//    
  }

}
