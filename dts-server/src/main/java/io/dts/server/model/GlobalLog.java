package io.dts.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author jiangyu.jy
 * 
 *         全局事务日志
 */
public class GlobalLog {

  /**
   * 事务ID
   */
  private long txId;

  /**
   * 事务状态
   */
  private int state;

  /**
   * 超时设置
   */
  private long timeout;

  /**
   * 创建时间
   */
  private Date gmtCreated;

  /**
   * 修改时间
   */
  private Date gmtModified;

  /**
   * 剩下未完成的分支数
   */
  private int leftBranches;

  /**
   * 应用名称
   */
  private String clientAppName;

  /**
   * 全局事务中是否有第二阶段提交的分支（典型为MT分支)
   */
  private boolean containPhase2CommitBranch = true;

  private List<Long> branchIds = Collections.synchronizedList(new ArrayList<Long>());

  /**
   * slave节点接收时间
   */
  private long recvTime;

  public long getRecvTime() {
    return recvTime;
  }

  public void setRecvTime(long recvTime) {
    this.recvTime = recvTime;
  }

  /**
   * @return
   */
  public boolean isContainPhase2CommitBranch() {
    return containPhase2CommitBranch;
  }

  /**
   * @param containPhase2CommitBranch
   */
  public void setContainPhase2CommitBranch(boolean containPhase2CommitBranch) {
    this.containPhase2CommitBranch = containPhase2CommitBranch;
  }

  /**
   * @return
   */
  public long getTxId() {
    return txId;
  }

  /**
   * @param txId
   */
  public void setTxId(long txId) {
    this.txId = txId;
  }

  /**
   * @return
   */
  public int getState() {
    return state;
  }

  /**
   * @param state
   */
  public void setState(int state) {
    this.state = state;
  }

  /**
   * @return
   */
  public Date getGmtCreated() {
    return gmtCreated;
  }

  /**
   * @return
   */
  public long getTimeout() {
    return timeout;
  }

  /**
   * @param timeout
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * @param gmtCreated
   */
  public void setGmtCreated(Date gmtCreated) {
    this.gmtCreated = gmtCreated;
  }

  /**
   * @return
   */
  public Date getGmtModified() {
    return gmtModified;
  }

  /**
   * @param gmtModified
   */
  public void setGmtModified(Date gmtModified) {
    this.gmtModified = gmtModified;
  }

  /**
   * @return
   */
  public int getLeftBranches() {
    return leftBranches;
  }

  /**
   * @param leftBranches
   */
  public void setLeftBranches(int leftBranches) {
    this.leftBranches = leftBranches;
  }

  /**
   * @return
   */
  public String getClientAppName() {
    return clientAppName;
  }

  /**
   * @param clientAppName
   */
  public void setClientAppName(String clientAppName) {
    this.clientAppName = clientAppName;
  }

  /**
   * 剩下分支数减一
   */
  public synchronized void decreaseLeftBranches() {
    if (this.leftBranches > 0)
      this.leftBranches--;
    else
      this.leftBranches = 0;
  }

  /**
   * 剩下分支数加一
   */
  public synchronized void increaseLeftBranches() {
    this.leftBranches++;
  }

  public List<Long> getBranchIds() {
    return branchIds;
  }

  public void setBranchIds(List<Long> branchIds) {
    this.branchIds = branchIds;
  }

  @Override
  public String toString() {
    return "tranId:" + this.txId + ",state:" + this.state + ",timeout:" + this.timeout
        + ",create time:" + this.gmtCreated + ",app name:" + this.clientAppName;
  }

}
