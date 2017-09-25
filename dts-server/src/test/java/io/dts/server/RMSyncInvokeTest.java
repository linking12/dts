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

import io.dts.common.common.CommitMode;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.remoting.RemotingClient;
import io.dts.remoting.protocol.RemotingCommand;

/**
 * @author liushiming
 * @version RMSyncInvokeTest.java, v 0.0.1 2017年9月25日 下午1:54:02 liushiming
 */
public class RMSyncInvokeTest {



  @Test
  public void test_RegisterMessage_Sync() throws Exception {
    RemotingClient client = NettyRPCTest.createRemotingClient();
    try {
      final RegisterMessage beginMessage = new RegisterMessage();
      beginMessage.setTranId(1l);
      byte commitMode = (byte) CommitMode.COMMIT_IN_PHASE2.getValue();
      beginMessage.setCommitMode(commitMode);
      RemotingCommand request = NettyRPCTest.buildRequest(beginMessage);
      RemotingCommand response = client.invokeSync("localhost:10086", request, 1000 * 3);
      RegisterResultMessage result = NettyRPCTest.buildResponse(response);
      System.out.println("invoke result = " + result);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    System.out.println("Resource Manager has started");
    while (true) {

    }

  }
}
