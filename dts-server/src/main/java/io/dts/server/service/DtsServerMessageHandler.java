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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.dts.common.protocol.body.BranchCommitResultMessage;
import io.dts.common.protocol.handler.DtsMsgHandler;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.common.protocol.header.ReportStatusMessage;
import io.dts.server.model.BranchLog;
import io.dts.server.model.GlobalLog;

/**
 * @author liushiming
 * @version DtsServerMessageListener.java, v 0.0.1 2017年9月6日 下午5:33:51 liushiming
 */
public class DtsServerMessageHandler implements DtsMsgHandler {

  /**
   * 当前活动的所有事务
   */
  private static Map<Long, GlobalLog> activeTranMap = new ConcurrentHashMap<Long, GlobalLog>();

  /**
   * 当前活动的所有事务分支
   */
  private static Map<Long, BranchLog> activeTranBranchMap =
      new ConcurrentHashMap<Long, BranchLog>();

  /**
   * 保存已经发送BranchCommitMessage消息，但是还没收到响应或者失败的分支
   */
  private static Map<Long, Integer> committingMap = new ConcurrentHashMap<Long, Integer>();

  /**
   * 保存已经发送BranchRollbackMessage消息，但是还没收到响应或者失败的分支
   */
  private static Map<Long, Integer> rollbackingMap = new ConcurrentHashMap<Long, Integer>();

  /**
   * 超时的事务列表
   */
  private static List<Long> timeoutTranList = Collections.synchronizedList(new ArrayList<Long>());

  @Override
  public void handleMessage(String clientIp, BeginMessage message,
      BeginResultMessage resultMessage) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(String clientIp, RegisterMessage message,
      RegisterResultMessage resultMessage) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(String clientIp, GlobalCommitMessage message,
      GlobalCommitResultMessage resultMessage) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(String clientIp, GlobalRollbackMessage message,
      BranchRollbackResultMessage resultMessage) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(String clientIp, BranchCommitResultMessage message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(String clientIp, BranchRollbackResultMessage message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleMessage(String clientIp, ReportStatusMessage message) {
    // TODO Auto-generated method stub

  }



}
