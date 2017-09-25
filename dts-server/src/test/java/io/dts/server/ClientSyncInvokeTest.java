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
package io.dts.server;

import org.junit.Test;

import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.GlobalRollbackResultMessage;
import io.dts.remoting.RemotingClient;
import io.dts.remoting.protocol.RemotingCommand;

/**
 * @author liushiming
 * @version ClientSyncInVokeTest.java, v 0.0.1 2017年9月25日 下午1:53:39 liushiming
 */
public class ClientSyncInvokeTest {


  @Test
  public void test_BeginMessage_Sync() throws Exception {
    RemotingClient client = NettyRPCTest.createRemotingClient();
    try {
      final BeginMessage beginMessage = new BeginMessage();
      beginMessage.setTimeout(3000);
      RemotingCommand request = NettyRPCTest.buildRequest(beginMessage);
      RemotingCommand response = client.invokeSync("localhost:10086", request, 1000 * 3);
      BeginResultMessage result = NettyRPCTest.buildResponse(response);
      System.out.println("invoke result = " + result);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    client.shutdown();
    System.out.println("-----------------------------------------------------------------");
  }

  @Test
  public void test_GlobalCommitMessage_Sync() throws Exception {
    RemotingClient client = NettyRPCTest.createRemotingClient();
    try {
      final GlobalCommitMessage globalCommitMessage = new GlobalCommitMessage();
      globalCommitMessage.setTranId(1l);
      RemotingCommand request = NettyRPCTest.buildRequest(globalCommitMessage);
      RemotingCommand response = client.invokeSync("localhost:10086", request, 1000 * 3);
      GlobalCommitResultMessage result = NettyRPCTest.buildResponse(response);
      System.out.println("invoke result = " + result);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    client.shutdown();
    System.out.println("-----------------------------------------------------------------");
  }

  @Test
  public void test_GlobalRollbackMessage_Sync() throws Exception {
    RemotingClient client = NettyRPCTest.createRemotingClient();
    try {
      final GlobalRollbackMessage globalRollbackMessage = new GlobalRollbackMessage();
      globalRollbackMessage.setTranId(1l);
      RemotingCommand request = NettyRPCTest.buildRequest(globalRollbackMessage);
      RemotingCommand response = client.invokeSync("localhost:10086", request, 1000 * 3);
      GlobalRollbackResultMessage result = NettyRPCTest.buildResponse(response);
      System.out.println("invoke result = " + result);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    client.shutdown();
    System.out.println("-----------------------------------------------------------------");
  }


}
