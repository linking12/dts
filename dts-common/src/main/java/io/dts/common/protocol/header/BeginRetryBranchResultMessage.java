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
import io.dts.remoting.annotation.CFNotNull;

/**
 * @author liushiming
 * @version BeginRetryBranchResultMessage.java, v 0.0.1 2017年9月4日 下午2:11:52 liushiming
 */
public class BeginRetryBranchResultMessage extends DtsMessage  {
  /**
   * 事务XID
   */
  @CFNotNull
  private String xid;
  /**
   * 分支ID
   */
  @CFNotNull
  private long branchId;

  public String getXid() {
    return xid;
  }

  public void setXid(String xid) {
    this.xid = xid;
  }

  public long getBranchId() {
    return branchId;
  }

  public void setBranchId(long branchId) {
    this.branchId = branchId;
  }

  @Override
  public short getTypeCode() {
    return TYPE_BEGIN_RETRY_BRANCH_RESULT;
  }


}
