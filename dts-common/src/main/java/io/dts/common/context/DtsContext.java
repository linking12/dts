package io.dts.common.context;

import io.dts.common.exception.DtsException;

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
		String num = (String) TxcContextOperateHelper.getUserData(BEGIN_COUNT);
		return num == null ? 0 : Integer.parseInt(num);
	}

	public static final int getCommitCount() {
		String num = (String) TxcContextOperateHelper.getUserData(COMMIT_COUNT);
		return num == null ? 0 : Integer.parseInt(num);
	}

	public static final void setBegin(int beginCount) {
		TxcContextOperateHelper.putUserData(BEGIN_COUNT, String.valueOf(beginCount));
	}

	public static final void setCommitCount(int commitCount) {
		TxcContextOperateHelper.putUserData(COMMIT_COUNT, String.valueOf(commitCount));
	}

	public static String getTxcNextSvrAddr() {
		return (String) TxcContextOperateHelper.getUserData(TXC_NEXT_SVR_ADDR);
	}

	public static final void clearReenterCounter() {
		TxcContextOperateHelper.removeUserData(COMMIT_COUNT);
		TxcContextOperateHelper.removeUserData(BEGIN_COUNT);
	}

	public static String getCurrentXid() {
		// return (String) ThreadLocalMap.get(TXC_CONTEXT_KEY);
		return (String) TxcContextOperateHelper.getUserData(TXC_XID_KEY);
	}

	public static void bind(String xid, String nextSvrAddr) throws DtsException {
		// ThreadLocalMap.put(TXC_CONTEXT_KEY, xid);
		// ThreadLocalMap.put(TXC_CONTEXT_MANAGER, "TXC");
		TxcContextOperateHelper.putUserData(TXC_XID_KEY, xid);
		TxcContextOperateHelper.putUserData(TXC_XID_OWNER_KEY, "TXC");
		if (nextSvrAddr != null)
			TxcContextOperateHelper.putUserData(TXC_NEXT_SVR_ADDR, nextSvrAddr);
	}


	public static boolean inTxcTransaction() {
		return getCurrentXid() != null;
	}


	public static void unbind() {
		// ThreadLocalMap.remove(TXC_CONTEXT_KEY);
		// ThreadLocalMap.remove(TXC_CONTEXT_MANAGER);
		String xid = getCurrentXid();

		TxcContextOperateHelper.removeUserData(TXC_XID_KEY);
		TxcContextOperateHelper.removeUserData(TXC_XID_OWNER_KEY);
		TxcContextOperateHelper.removeUserData(TXC_NEXT_SVR_ADDR);
	}

	public static String suspendTxcTransaction() {
		return (String) TxcContextOperateHelper.removeUserData(TXC_XID_KEY);
	}

	public static void resumeTxcTransaction(String xid) {
		if (xid != null)
			TxcContextOperateHelper.putUserData(TXC_XID_KEY, xid);
	}

	public static void startRetryBranch(long effectiveTime) {
		TxcContextOperateHelper.putUserData(TXC_RETRY_BRANCH_KEY, Long.toString(effectiveTime));
	}

	public static void endRetryBranch() {
		TxcContextOperateHelper.removeUserData(TXC_RETRY_BRANCH_KEY);
	}

	public static boolean inRetryContext() {
		return TxcContextOperateHelper.getUserData(TXC_RETRY_BRANCH_KEY) != null;
	}

	public static long getEffectiveTime() {
		String s = TxcContextOperateHelper.getUserData(TXC_RETRY_BRANCH_KEY);
		if (s != null) {
			return Long.parseLong(s);
		} else
			return -1;
	}

	public static void putMtUdata(String mtUdata) {
		if (mtUdata != null)
			TxcContextOperateHelper.putUserData(MT_UDATA_KEY, mtUdata);
	}

	public static String removeMtUdata() {
		return (String) TxcContextOperateHelper.removeUserData(MT_UDATA_KEY);
	}

	public static boolean inTxcEnv() {
		return DtsContext.inTxcTransaction() || DtsContext.inRetryContext();
	}
}