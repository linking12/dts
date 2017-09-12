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
import io.dts.remoting.CommandCustomHeader;
import io.dts.remoting.exception.RemotingCommandException;

/**
 * @author liushiming
 * @version ReportUdataMessage.java, v 0.0.1 2017年9月1日 下午6:26:45 liushiming
 */
public class ReportUdataMessage implements CommandCustomHeader, DtsMessage {

  /**
   * 事务ID
   */
  long tranId;

  /**
   * 分支ID
   */
  long branchId;

  String key;

  /**
   * 用户自定义信息，MT服务可以把一阶段的一些用户数据上报给Server，Server在二阶段把这个信息再传下来； 这样MT服务二阶段可以节省一次查询
   */
  String udata = null;

  public long getTranId() {
    return tranId;
  }

  public void setTranId(long tranId) {
    this.tranId = tranId;
  }

  public long getBranchId() {
    return branchId;
  }

  public void setBranchId(long branchId) {
    this.branchId = branchId;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getUdata() {
    return udata;
  }

  public void setUdata(String udata) {
    this.udata = udata;
  }

  @Override
  public short getTypeCode() {
    return TYPE_REPORT_UDATA_RESULT;
  }

  @Override
  public void checkFields() throws RemotingCommandException {
    // TODO Auto-generated method stub
    
  }


}
