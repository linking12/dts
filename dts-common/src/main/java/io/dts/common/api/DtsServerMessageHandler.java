package io.dts.common.api;

import io.dts.common.protocol.header.BeginMessage;
import io.dts.common.protocol.header.BeginResultMessage;
import io.dts.common.protocol.header.GlobalCommitMessage;
import io.dts.common.protocol.header.GlobalCommitResultMessage;
import io.dts.common.protocol.header.GlobalRollbackMessage;
import io.dts.common.protocol.header.GlobalRollbackResultMessage;
import io.dts.common.protocol.header.RegisterMessage;
import io.dts.common.protocol.header.RegisterResultMessage;


public interface DtsServerMessageHandler {

  /**
   * 开始一个分布式事务
   * 
   */
  public void handleMessage(String clientIp, BeginMessage message,
      BeginResultMessage resultMessage);

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
      GlobalRollbackResultMessage resultMessage);

  /**
   * 处理事务分支注册
   * 
   */
  public void handleMessage(String clientIp, RegisterMessage message,
      RegisterResultMessage resultMessage);



}
