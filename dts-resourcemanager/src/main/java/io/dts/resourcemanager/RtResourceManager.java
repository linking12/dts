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
package io.dts.resourcemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.common.CommitMode;
import io.dts.common.common.context.DtsContext;
import io.dts.common.protocol.header.BeginRetryBranchMessage;
import io.dts.common.protocol.header.BeginRetryBranchResultMessage;
import io.dts.resourcemanager.api.IDtsDataSource;

/**
 * @author liushiming
 * @version RtResourceManager.java, v 0.0.1 2017年10月16日 下午3:18:54 liushiming
 */
public class RtResourceManager extends AtResourceManager {
  private static final Logger logger = LoggerFactory.getLogger(RtResourceManager.class);

  public BeginRetryBranchResultMessage beginRtBranch(IDtsDataSource dataSource, String sql) {
    return this.beginRtBranch(dataSource, sql, 10);
  }

  public BeginRetryBranchResultMessage beginRtBranch(IDtsDataSource dataSource, String sql,
      int retryTimes) {
    BeginRetryBranchMessage message = new BeginRetryBranchMessage();
    BeginRetryBranchResultMessage resultMessage = null;
    message.setDbName(dataSource.getDbName());
    message.setSql(sql);
    long l = DtsContext.getEffectiveTime();
    if (l < 0)
      return null;
    message.setEffectiveTime(l);
    message.setCommitMode((byte) CommitMode.COMMIT_RETRY_MODE.getValue());
    int i = 0;
    while (i < retryTimes) {
      try {
        resultMessage = (BeginRetryBranchResultMessage) super.invoke(message);
        return resultMessage;
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
    }

    return resultMessage;
  }

}
