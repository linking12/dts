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
package io.dts.server.service;

import io.dts.common.protocol.ResultMessage;
import io.dts.common.protocol.body.DtsMergeMessage;
import io.dts.common.protocol.handler.DtsMsgHandler;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginRetryBranchMessage;
import io.dts.common.protocol.header.BranchCommitResultMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.QueryLockMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.ReportStatusMessage;
import io.dts.common.protocol.header.ReportUdataMessage;

/**
 * @author liushiming
 * @version DtsServerMessageListener.java, v 0.0.1 2017年9月6日 下午5:33:51 liushiming
 */
public class DtsServerMessageHandler implements DtsMsgHandler {

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BeginMessage message, ResultMessage[] results, int idx) {

  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BranchCommitResultMessage message, ResultMessage[] results, int idx) {

  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BranchRollbackResultMessage message, ResultMessage[] results, int idx) {

  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      GlobalCommitMessage message, ResultMessage[] results, int idx) {

  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      GlobalRollbackMessage message, ResultMessage[] results, int idx) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      RegisterMessage message, ResultMessage[] results, int idx) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      ReportStatusMessage message, ResultMessage[] results, int idx) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BeginRetryBranchMessage message, ResultMessage[] results, int idx) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      ReportUdataMessage message, ResultMessage[] results, int idx) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      DtsMergeMessage message, ResultMessage[] results, int idx) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      QueryLockMessage message, ResultMessage[] results, int idx) {
    // TODO Auto-generated method stub

  }



}
