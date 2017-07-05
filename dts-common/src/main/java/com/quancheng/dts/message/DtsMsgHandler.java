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
package com.quancheng.dts.message;

import com.quancheng.dts.message.request.BeginMessage;
import com.quancheng.dts.message.request.BeginRetryBranchMessage;
import com.quancheng.dts.message.request.GlobalCommitMessage;
import com.quancheng.dts.message.request.GlobalRollbackMessage;
import com.quancheng.dts.message.request.QueryLockMessage;
import com.quancheng.dts.message.request.RegisterMessage;
import com.quancheng.dts.message.request.ReportStatusMessage;
import com.quancheng.dts.message.request.ReportUdataMessage;
import com.quancheng.dts.message.response.BranchCommitResultMessage;
import com.quancheng.dts.message.response.BranchRollbackResultMessage;
import com.quancheng.dts.message.response.ResultMessage;

/**
 * 
 * @author liushiming
 * @version DtsMsgHandler.java, v 0.0.1 2017年7月5日 下午5:33:53 liushiming
 * @since JDK 1.8
 */
public interface DtsMsgHandler {

  /**
   * 开启一个分布式事务
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BeginMessage message, ResultMessage[] results, int idx);

  /**
   * 处理事务分支提交的反馈结果
   * 
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BranchCommitResultMessage message, ResultMessage[] results, int idx);

  /**
   * 处理事务分支回滚的反馈结果
   * 
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BranchRollbackResultMessage message, ResultMessage[] results, int idx);

  /**
   * 处理全局事务提交
   * 
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      GlobalCommitMessage message, ResultMessage[] results, int idx);

  /**
   * 处理全局事务回滚
   * 
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      GlobalRollbackMessage message, ResultMessage[] results, int idx);

  /**
   * 处理事务分支注册
   * 
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      RegisterMessage message, ResultMessage[] results, int idx);

  /**
   * 事务分支上报状态消息处理
   * 
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      ReportStatusMessage message, ResultMessage[] results, int idx);

  /**
   * 可重试事务分支处理
   * 
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BeginRetryBranchMessage message, ResultMessage[] results, int idx);

  /**
   * 事务分支上报用户数据（udata）消息处理
   * 
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      ReportUdataMessage message, ResultMessage[] results, int idx);

  /**
   * 查询锁是否占用消息处理
   * 
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      QueryLockMessage message, ResultMessage[] results, int idx);

  /**
   * 合并消息的处理
   * 
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      DtsMergeMessage message, ResultMessage[] results, int idx);


}
