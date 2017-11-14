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
   * 客户端应用
   */
  private String clientAppName;

  /**
   * 客户端信息，如dbkey
   */
  private String clientInfo;

  /**
   * 用户自定义信息
   */
  private String udata;

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
   * 当事务超时时，如果有的分支状态还没有上报，则需要等待N个周期。这个属性记录已等待的周期数。
   */
  private int waitPeriods;

  /**
   * slave节点接收时间
   */
  private long recvTime;

  /**
   * 业务主键，用于强隔离。分支上报给server，自己修改了哪些表的哪些行的主键。格式如下： "tableName1:key1,key2,key3;tableName2:key1,key2"
   */
  String businessKey;

  public long getRecvTime() {
    return recvTime;
  }

  public void setRecvTime(long recvTime) {
    this.recvTime = recvTime;
  }

  /**
   * @return
   */
  public int getWaitPeriods() {
    return waitPeriods;
  }

  /**
   * @param waitPeriods
   */
  public void setWaitPeriods(int waitPeriods) {
    this.waitPeriods = waitPeriods;
  }

  /**
   * increte wait periods
   */
  public void incWaitPeriods() {
    this.waitPeriods++;
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
  public String getUdata() {
    return udata;
  }

  /**
   * @param udata
   */
  public void setUdata(String udata) {
    this.udata = udata;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "tranId:" + this.transId + ",branchId:" + this.branchId + ",state:" + this.state
        + ",udata:" + this.udata;
  }
}
