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
package com.quancheng.dts.common;

/**
 * @author liushiming
 * @version DtsErrorCode.java, v 0.0.1 2017年7月24日 下午2:54:04 liushiming
 * @since JDK 1.8
 */
public enum DtsErrCode {
  /**
   * 0001 ~ 0099  与配置相关的错误
   */

  DiamondGetConfig("0001", "diamond.getconfig 执行错误", "检查与diamond的连接"),
  DiamondGetDbNull("0002", "在diamond 中获取的数据库key为空", "检查diamond上的数据"),
  DiamondGetGroupNull("0003", "在diamond 中取出取出的group信息为空", "检查diamond上的数据"),
  ThreadPoolFull("0004", "netty线程池满", "请在配置文件中调整线程数， corePoolSize 的值调大一些"),
  MTResourceManagerNotDefined("0005","mt模式资源管理器未定义",""),
  InstanceNotFoundInSpringContext("0006","bean name为%s的实例未在spring上下文中定义",""),
  DiamondWriteError("0007","diamond 写入错误，mt服务中止",""),

  /**
   * 0101 ~ 0199 网络有关的错误，连接不上，断开，dispatch等
   */
  NetConnect("0101", "无法连接服务器", "请检查txc server是否启动，到txc server的网络连接是否正常"),
  NetRegAppname("0102", "register client app name failed", "请检查txc server是否启动，到txc server的网络连接是否正常"),
  NetDisconnect("0103", "txcConnection closed", "网络断开，请检查到对端（client 或txc server）的网络连接"),
  NetDispatch("0104", "dispatch 错误", "网络处理错误，请检查到对端（client 或txc server）的网络连接 "),
  NetOnMessage("0105", "on message 错误", "网络处理错误，请检查到对端（client 或txc server）的网络连接 "),
  
  /**
   * 0201 ~ 0299 数据库相关的错误，连接错误，数据库表结构错误，执行sql语句错误等
   */
  DBUndologNotExist("0201", "表 txc_undo_log 不存在", "检查txc server连接数据库的表结构"),
  DBSQlType("0202", "sql 类型错误，应为 delete update insert 之一", "sql语句不支持，请修改"),
  DBTableNotExist("0203", "数据库表不存在", "请检查数据库表"),
  DBRedoSyncToTableError("0204","同步真实表错误","请查看表结构是否修改"),
  
  
  /**
   * 0301 ~ 0399 txc 业务错误及其他错误
   */
  Sleep("0301", "sleep 时出错", ""),
  CreateRandom("0302", "创建随机数失败", ""),
  NoRegistAppname("0303", "client 未注册appname", ""),
  RegistRM("0304", "注册RM失败", ""),
  SqlCache("0305", "sql cache 出错", ""),
  TransactionChecker("0306", "事物检查错误", ""),
  BeginRtBranch("0307", "开启rt分支错误", ""),
  HandleBranchRollBack("0308", "处理分支回滚错误", ""),
  HandleBranchCommit("0309", "处理分支提交错误", ""),
  DeleteBranchLog("0310", "删除分支日志错误", ""),
  InsertBranchLog("0311", "插入分支日志错误", ""),
  UpdateBranchLog("0312", "更新分支日志错误", ""),
  GetBranchLogs("0313", "获取分支日志错误", ""),  
  RollBackLogic("0314", "分支回滚逻辑错误", ""),
  UpdateGlobalLog("0315", "更新全局日志错误", ""),
  SyncGlobalRollback("0316", "同步全局回滚错误", ""),
  DeleteGlobalLog("0317", "删除全局日志错误", ""),
  ExceptionCaught("0318", "异常", ""),
  ServerSendRequest("0319", "server 发送请求失败", ""),
  ServerSendResponse("0320", "server发送响应错误", ""),
  InsertGlobalLog("0321", "插入全局日志错误", ""),
  DumpTxcLog("0322", "转储TXC事务日志错误", ""),
  ClusterSyncLog("0323", "主备节点同步错误", ""),
  AlarmError("0324","报警日志错误",""),
  Diagnose("0325", "诊断性错误", ""),
  
  Fatal("9999","未预期异常","");
  public String errCode;
  public String errMessage;
  public String errDispose;
  private DtsErrCode(String errCode, String errMessage, String errDispose){
      this.errCode = errCode;
      this.errMessage = errMessage;
      this.errDispose = errDispose;
  }
  
}
