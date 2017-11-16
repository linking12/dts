package io.dts.resourcemanager.struct;


public class ContextStep2 {
  private long globalXid;
  private String xid;
  private long branchId;
  private String dbname;

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



  public String getDbname() {
    return dbname;
  }

  public void setDbname(String dbname) {
    this.dbname = dbname;
  }


}
