package io.dts.common.common.context;

import io.dts.common.common.exception.DtsException;

public class DtsContext {
	
	// private static final String TXC_CONTEXT_KEY = "TXC_CONTEXT";
	// private static final String TXC_CONTEXT_MANAGER = "TXC_CONTEXT_MANAGER";
	public static final String TXC_XID_KEY = "TXC_XID";
	public static final String TXC_XID_OWNER_KEY = "TXC_XID_OWNER";
	public static final String TXC_RETRY_BRANCH_KEY = "TXC_RETRY_BRANCH";
	public static final String MT_UDATA_KEY = "MT_UDATA";

	public static final String COMMIT_COUNT = "COMMIT_COUNT";
	public static final String BEGIN_COUNT = "BEGIN_COUNT";

	public static final String TXC_NEXT_SVR_ADDR = "TXC_NEXT_SVR_ADDR";
	
	public static final String NEW_REDO_CONTEXT = "TXC_NEW_REDO_CONTEXT";

	public static final int getBeginCount() {
		String num = (String) ContextOperateHelper.getUserData(BEGIN_COUNT);
		return num == null ? 0 : Integer.parseInt(num);
	}

	public static final int getCommitCount() {
		String num = (String) ContextOperateHelper.getUserData(COMMIT_COUNT);
		return num == null ? 0 : Integer.parseInt(num);
	}

	public static final void setBegin(int beginCount) {
		ContextOperateHelper.putUserData(BEGIN_COUNT, String.valueOf(beginCount));
	}

	public static final void setCommitCount(int commitCount) {
		ContextOperateHelper.putUserData(COMMIT_COUNT, String.valueOf(commitCount));
	}

	public static String getTxcNextSvrAddr() {
		return (String) ContextOperateHelper.getUserData(TXC_NEXT_SVR_ADDR);
	}

	public static final void clearReenterCounter() {
		ContextOperateHelper.removeUserData(COMMIT_COUNT);
		ContextOperateHelper.removeUserData(BEGIN_COUNT);
	}

	public static String getCurrentXid() {
		// return (String) ThreadLocalMap.get(TXC_CONTEXT_KEY);
		return (String) ContextOperateHelper.getUserData(TXC_XID_KEY);
	}

	public static void bind(String xid, String nextSvrAddr) throws DtsException {
		// ThreadLocalMap.put(TXC_CONTEXT_KEY, xid);
		// ThreadLocalMap.put(TXC_CONTEXT_MANAGER, "TXC");
		ContextOperateHelper.putUserData(TXC_XID_KEY, xid);
		ContextOperateHelper.putUserData(TXC_XID_OWNER_KEY, "TXC");
		if (nextSvrAddr != null)
			ContextOperateHelper.putUserData(TXC_NEXT_SVR_ADDR, nextSvrAddr);
	}


	public static boolean inTxcTransaction() {
		return getCurrentXid() != null;
	}


	public static void unbind() {
		// ThreadLocalMap.remove(TXC_CONTEXT_KEY);
		// ThreadLocalMap.remove(TXC_CONTEXT_MANAGER);
		String xid = getCurrentXid();

		ContextOperateHelper.removeUserData(TXC_XID_KEY);
		ContextOperateHelper.removeUserData(TXC_XID_OWNER_KEY);
		ContextOperateHelper.removeUserData(TXC_NEXT_SVR_ADDR);
	}

	public static String suspendTxcTransaction() {
		return (String) ContextOperateHelper.removeUserData(TXC_XID_KEY);
	}

	public static void resumeTxcTransaction(String xid) {
		if (xid != null)
			ContextOperateHelper.putUserData(TXC_XID_KEY, xid);
	}

	public static void startRetryBranch(long effectiveTime) {
		ContextOperateHelper.putUserData(TXC_RETRY_BRANCH_KEY, Long.toString(effectiveTime));
	}

	public static void endRetryBranch() {
		ContextOperateHelper.removeUserData(TXC_RETRY_BRANCH_KEY);
	}

	public static boolean inRetryContext() {
		return ContextOperateHelper.getUserData(TXC_RETRY_BRANCH_KEY) != null;
	}

	public static long getEffectiveTime() {
		String s = ContextOperateHelper.getUserData(TXC_RETRY_BRANCH_KEY);
		if (s != null) {
			return Long.parseLong(s);
		} else
			return -1;
	}

	public static void putMtUdata(String mtUdata) {
		if (mtUdata != null)
			ContextOperateHelper.putUserData(MT_UDATA_KEY, mtUdata);
	}

	public static String removeMtUdata() {
		return (String) ContextOperateHelper.removeUserData(MT_UDATA_KEY);
	}

	public static boolean inTxcEnv() {
		return DtsContext.inTxcTransaction() || DtsContext.inRetryContext();
	}
}