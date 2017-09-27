package io.dts.resourcemanager.remoting.receiver;

import io.dts.common.protocol.header.BranchCommitMessage;
import io.dts.common.protocol.header.BranchCommitResultMessage;
import io.dts.common.protocol.header.BranchRollBackMessage;
import io.dts.common.protocol.header.BranchRollbackResultMessage;

/**
 * @author jiangyu.jy
 * 
 *         TXC 消息处理器
 */
public class DtsRmMessageHandler {

  public void handleMessage(final String clientIp, final BranchCommitMessage message,
      final BranchCommitResultMessage resultMessage) {

  }

  public void handleMessage(final String clientIp, final BranchRollBackMessage message,
      final BranchRollbackResultMessage resultMessage) {

  }



}
