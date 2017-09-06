package io.dts.common.protocol.handler;

import io.dts.common.protocol.ResultMessage;
import io.dts.common.protocol.body.DtsMergeMessage;
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
 * @author jiangyu.jy
 * 
 *         TXC 消息处理器
 */
public interface DtsMsgHandler extends MsgHandler {
  /**
   * 开始一个分布式事务
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BeginMessage message, ResultMessage[] results, int idx);

  /**
   * 处理事务分支提交的反馈结果
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BranchCommitResultMessage message, ResultMessage[] results, int idx);

  /**
   * 处理事务分支回滚的反馈结果。
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BranchRollbackResultMessage message, ResultMessage[] results, int idx);

  /**
   * 处理全局事务提交
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      GlobalCommitMessage message, ResultMessage[] results, int idx);

  /**
   * 处理全局事务回滚
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      GlobalRollbackMessage message, ResultMessage[] results, int idx);

  /**
   * 处理事务分支注册
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      RegisterMessage message, ResultMessage[] results, int idx);

  /**
   * 事务分支上报状态消息处理
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      ReportStatusMessage message, ResultMessage[] results, int idx);

  /**
   * 可重试事务分支处理
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      BeginRetryBranchMessage message, ResultMessage[] results, int idx);

  /**
   * 事务分支上报用户数据（udata）消息处理
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      ReportUdataMessage message, ResultMessage[] results, int idx);

  /**
   * 合并消息的处理
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      DtsMergeMessage message, ResultMessage[] results, int idx);

  /**
   * 查询锁是否占用消息处理
   * 
   * @param msgId
   * @param dbKeys
   * @param clientIp
   * @param clientAppName
   * @param message
   * @param results
   * @param idx
   * @param results
   * @param idx
   */
  public void handleMessage(long msgId, String dbKeys, String clientIp, String clientAppName,
      QueryLockMessage message, ResultMessage[] results, int idx);
}
