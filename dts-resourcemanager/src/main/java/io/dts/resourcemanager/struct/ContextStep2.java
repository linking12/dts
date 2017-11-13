package io.dts.resourcemanager.struct;

import io.dts.common.common.CommitMode;

public class ContextStep2 {
  private long globalXid;
  private String xid;
  private long branchId;
  private String dbname;
  private String udata;
  private CommitMode commitMode;
  private String retrySql;
  private String reportSql;

  public long getGlobalXid() {
    return globalXid;
  }

  public void setGlobalXid(long globalXid) {
    this.globalXid = globalXid;
  }

  public String getXid() {
    return xid;
  }

  public void setXid(String xid) {
    this.xid = xid;
  }

  public long getBranchId() {
    return branchId;
  }

  public void setBranchId(long branchId) {
    this.branchId = branchId;
  }

  public String getUdata() {
    return udata;
  }

  public void setUdata(String udata) {
    this.udata = udata;
  }

  public CommitMode getCommitMode() {
    return commitMode;
  }

  public void setCommitMode(CommitMode commitMode) {
    this.commitMode = commitMode;
  }

  public String getRetrySql() {
    return retrySql;
  }

  public void setRetrySql(String retrySql) {
    this.retrySql = retrySql;
  }

  public String getDbname() {
    return dbname;
  }

  public void setDbname(String dbname) {
    this.dbname = dbname;
  }

  public String getReportSql() {
    return reportSql;
  }

  public void setReportSql(String reportSql) {
    this.reportSql = reportSql;
  }



}
