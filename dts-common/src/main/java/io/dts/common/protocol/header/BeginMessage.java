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
package io.dts.common.protocol.header;

import io.dts.common.protocol.RequestMessage;
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.annotation.CFNotNull;
import io.dts.remoting.exception.RemotingCommandException;

/**
 * 开始事务消息
 * 
 * @author liushiming
 * @version BeginMessage.java, v 0.0.1 2017年9月1日 下午5:45:54 liushiming
 */
public class BeginMessage implements CommandCustomHeader, RequestMessage {

  @CFNotNull
  public long timeout = 60000;

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  @Override
  public void checkFields() throws RemotingCommandException {

  }


}
