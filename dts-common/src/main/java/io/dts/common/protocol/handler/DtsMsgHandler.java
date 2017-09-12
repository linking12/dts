package io.dts.common.protocol.handler;

import io.dts.common.protocol.body.BranchCommitResultMessage;
import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;
import io.dts.common.protocol.header.ReportStatusMessage;

/**
 * @author jiangyu.jy
 * 
 *         TXC 消息处理器
 */
public interface DtsMsgHandler extends MsgHandler {
  /**
   * 开始一个分布式事务
   * 
   */
  public void handleMessage(String clientIp, BeginMessage message,
      BeginResultMessage resultMessage);

  /**
   * 处理事务分支注册
   * 
   */
  public void handleMessage(String clientIp, RegisterMessage message,
      RegisterResultMessage resultMessage);


  /**
   * 处理全局事务提交
   * 
   */
  public void handleMessage(String clientIp, GlobalCommitMessage message,
      GlobalCommitResultMessage resultMessage);

  /**
   * 处理全局事务回滚
   * 
   */
  public void handleMessage(String clientIp, GlobalRollbackMessage message,
      BranchRollbackResultMessage resultMessage);

  /**
   * 处理事务分支提交的反馈结果
   * 
   */
  public void handleMessage(String clientIp, BranchCommitResultMessage message);

  /**
   * 处理事务分支回滚的反馈结果。
   * 
   */
  public void handleMessage(String clientIp, BranchRollbackResultMessage message);



  /**
   * 事务分支上报状态消息处理
   * 
   */
  public void handleMessage(String clientIp, ReportStatusMessage message);

}
