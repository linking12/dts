package io.dts.server.struct;

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
  private long transId;

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


  public long getTransId() {
    return transId;
  }

  public void setTransId(long transId) {
    this.transId = transId;
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
    return branchIds.size();
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


  public List<Long> getBranchIds() {
    return branchIds;
  }

  public void setBranchIds(List<Long> branchIds) {
    this.branchIds = branchIds;
  }

  @Override
  public String toString() {
    return "tranId:" + this.transId + ",state:" + this.state + ",timeout:" + this.timeout
        + ",create time:" + this.gmtCreated + ",app name:" + this.clientAppName;
  }

}
