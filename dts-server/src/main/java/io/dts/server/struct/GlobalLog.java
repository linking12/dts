package io.dts.server.struct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 全局事务日志
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
