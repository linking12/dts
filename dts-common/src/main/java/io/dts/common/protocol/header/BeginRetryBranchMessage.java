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
import io.dts.remoting.annotation.CFNotNull;
import io.dts.remoting.exception.RemotingCommandException;

/**
 * @author liushiming
 * @version BeginRetryBranchMessage.java, v 0.0.1 2017年9月1日 下午5:46:06 liushiming
 */
public class BeginRetryBranchMessage implements CommandCustomHeader, DtsMessage {
  /**
   * 有效时长；超出这个时长，转为告警通知用户。 缺省半小时
   */
  @CFNotNull
  private long effectiveTime = 30 * 60000;
  /**
   * 完整DB name串
   */
  @CFNotNull
  private String dbName;
  /**
   * 提交模式
   */
  @CFNotNull
  private byte commitMode;

  /**
   * 重试的SQL
   */
  @CFNotNull
  private String sql;

  public long getEffectiveTime() {
    return effectiveTime;
  }

  public void setEffectiveTime(long effectiveTime) {
    this.effectiveTime = effectiveTime;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public byte getCommitMode() {
    return commitMode;
  }

  public void setCommitMode(byte commitMode) {
    this.commitMode = commitMode;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  @Override
  public short getTypeCode() {
    return TYPE_BEGIN_RETRY_BRANCH;
  }

  @Override
  public void checkFields() throws RemotingCommandException {
    // TODO Auto-generated method stub

  }


}
