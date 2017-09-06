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

import io.dts.common.protocol.DtsMessage;

/**
 * @author liushiming
 * @version GlobalCommitResultMessage.java, v 0.0.1 2017年9月4日 下午2:34:10 liushiming
 */
public class GlobalCommitResultMessage extends DtsMessage {
  private long tranId;

  public long getTranId() {
    return tranId;
  }

  public void setTranId(long tranId) {
    this.tranId = tranId;
  }

  @Override
  public short getTypeCode() {
    return TYPE_GLOBAL_COMMIT_RESULT;
  }



}
