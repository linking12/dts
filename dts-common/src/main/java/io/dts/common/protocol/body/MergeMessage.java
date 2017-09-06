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
package io.dts.common.protocol.body;

import java.util.ArrayList;
import java.util.List;

import io.dts.common.protocol.DtsMessage;
import io.dts.remoting.protocol.RemotingSerializable;

/**
 * @author liushiming
 * @version DtsMergeMessage.java, v 0.0.1 2017年9月4日 下午4:27:31 liushiming
 */
public class MergeMessage extends RemotingSerializable {

  public List<DtsMessage> msgs = new ArrayList<DtsMessage>();

  public List<Long> msgIds = new ArrayList<Long>();

  public List<DtsMessage> getMsgs() {
    return msgs;
  }

  public void setMsgs(List<DtsMessage> msgs) {
    this.msgs = msgs;
  }

  public List<Long> getMsgIds() {
    return msgIds;
  }

  public void setMsgIds(List<Long> msgIds) {
    this.msgIds = msgIds;
  }



}
