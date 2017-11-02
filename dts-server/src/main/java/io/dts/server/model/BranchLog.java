package io.dts.server.model;

import java.util.Date;

/**
 * @author jiangyu.jy
 * 
 *         事务分支日志
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
   * 提交模式 （TXC缺省RM都是一阶段做事务分支的提交，用户自定义RM，如MT场景为第二阶段提交
   */
  private int commitMode;

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
   * 用户自定义信息，MT服务可以把一阶段的一些用户数据上报给Server，Server在二阶段把这个信息再传下来； 这样MT服务二阶段可以节省一次查询
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
   * 是否删除事务锁， 该标记适用于读已提交隔离级别的回滚逻辑，对于读未提交的情况不起作用
   */
  private int isDelLock;

  /**
   * 重试Sql
   */
  private String retrySql;

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

  public String getRetrySql() {
    return retrySql;
  }

  public void setRetrySql(String retrySql) {
    this.retrySql = retrySql;
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

  /**
   * @return
   */
  public int getCommitMode() {
    return commitMode;
  }

  /**
   * @param commitMode
   */
  public void setCommitMode(int commitMode) {
    this.commitMode = commitMode;
  }

  public int getIsDelLock() {
    return isDelLock;
  }

  public void setIsDelLock(int isDelLock) {
    this.isDelLock = isDelLock;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "tranId:" + this.transId + ",branchId:" + this.branchId + ",state:" + this.state
        + ",commit mode:" + this.commitMode + ",udata:" + this.udata + ",retrySql:" + this.retrySql
        + ",isDelLock:" + this.isDelLock;
  }
}
