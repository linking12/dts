package io.dts.server.struct;

import java.util.Date;

/**
 * 
 * 事务分支日志
 */
public class BranchLog {

  /**
   * 分支ID
   */
  private long branchId;

  /**
   * 事务ID
   */
  private long transId;

  /**
   * 状态
   */
  private int state;

  /**
   * 客户端IP
   */
  private String clientIp;

  /**
   * 客户端信息，如dbkey
   */
  private String clientInfo;

  /**
   * 创建时间
   */
  private Date gmtCreated;

  /**
   * 修改时间
   */
  private Date gmtModified;

  /**
   * 是否通知客户错误
   */
  private int isNotify;


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
  public int getIsNotify() {
    return isNotify;
  }

  /**
   * @param isNotify
   */
  public void setIsNotify(int isNotify) {
    this.isNotify = isNotify;
  }

  /**
   * @return
   */
  public long getBranchId() {
    return branchId;
  }

  /**
   * @param branchId
   */
  public void setBranchId(long branchId) {
    this.branchId = branchId;
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
  public String getClientIp() {
    return clientIp;
  }

  /**
   * @param clientIp
   */
  public void setClientIp(String clientIp) {
    this.clientIp = clientIp;
  }

  /**
   * @return
   */
  public String getClientInfo() {
    return clientInfo;
  }

  /**
   * @param clientInfo
   */
  public void setClientInfo(String clientInfo) {
    this.clientInfo = clientInfo;
  }

  /**
   * @return
   */
  public Date getGmtCreated() {
    return gmtCreated;
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

  @Override
  public String toString() {
    return "BranchLog [branchId=" + branchId + ", transId=" + transId + ", state=" + state
        + ", clientIp=" + clientIp + ", clientInfo=" + clientInfo + ", gmtCreated=" + gmtCreated
        + ", gmtModified=" + gmtModified + ", isNotify=" + isNotify + ", recvTime=" + recvTime
        + "]";
  }

}
