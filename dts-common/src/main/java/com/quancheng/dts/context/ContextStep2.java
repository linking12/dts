package com.quancheng.dts.context;

import com.quancheng.dts.common.CommitMode;
import com.quancheng.dts.common.TrxLockMode;

public class ContextStep2 {
	private long globalXid;
	private String xid;
	private long branchId;
	private String dbname;
	private String udata;
	private CommitMode commitMode;
	private String retrySql;
	private TrxLockMode lockMode;

	public TrxLockMode getLockMode() {
		return lockMode;
	}

	public void setLockMode(TrxLockMode lockMode) {
		this.lockMode = lockMode;
	}

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

}
