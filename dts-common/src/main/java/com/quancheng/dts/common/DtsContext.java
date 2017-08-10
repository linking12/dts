package com.quancheng.dts.common;

import java.util.concurrent.ConcurrentHashMap;

import com.quancheng.dts.context.DtsContextOperateHelper;

public class DtsContext {

  private static final ConcurrentHashMap<String, RedoContext> REDO_CONTEXT_MAP =
      new ConcurrentHashMap<String, RedoContext>();
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
    String num = (String) DtsContextOperateHelper.getUserData(BEGIN_COUNT);
    return num == null ? 0 : Integer.parseInt(num);
  }

  public static final int getCommitCount() {
    String num = (String) DtsContextOperateHelper.getUserData(COMMIT_COUNT);
    return num == null ? 0 : Integer.parseInt(num);
  }

  public static final void setBegin(int beginCount) {
    DtsContextOperateHelper.putUserData(BEGIN_COUNT, String.valueOf(beginCount));
  }

  public static final void setCommitCount(int commitCount) {
    DtsContextOperateHelper.putUserData(COMMIT_COUNT, String.valueOf(commitCount));
  }

  public static String getTxcNextSvrAddr() {
    return (String) DtsContextOperateHelper.getUserData(TXC_NEXT_SVR_ADDR);
  }

  public static final void clearReenterCounter() {
    DtsContextOperateHelper.removeUserData(COMMIT_COUNT);
    DtsContextOperateHelper.removeUserData(BEGIN_COUNT);
  }

  public static String getCurrentXid() {
    // return (String) ThreadLocalMap.get(TXC_CONTEXT_KEY);
    return (String) DtsContextOperateHelper.getUserData(TXC_XID_KEY);
  }

  public static void bind(String xid, String nextSvrAddr) throws DtsException {
    // ThreadLocalMap.put(TXC_CONTEXT_KEY, xid);
    // ThreadLocalMap.put(TXC_CONTEXT_MANAGER, "TXC");
    DtsContextOperateHelper.putUserData(TXC_XID_KEY, xid);
    DtsContextOperateHelper.putUserData(TXC_XID_OWNER_KEY, "TXC");
    REDO_CONTEXT_MAP.put(xid, new RedoContext());
    if (nextSvrAddr != null)
      DtsContextOperateHelper.putUserData(TXC_NEXT_SVR_ADDR, nextSvrAddr);
  }

  static public void check() {
    if (DtsContext.inTxcTransaction() && DtsContext.inRetryContext()) {
      throw new DtsException("Both in AT/MT & RT is not support!");
    }
  }

  public static boolean inTxcTransaction() {
    return getCurrentXid() != null;
  }

  public static RedoContext getRedoContext() {
    try {
      return REDO_CONTEXT_MAP.get(getCurrentXid());
    } catch (NullPointerException e) {
      throw e;
    }
  }

  public static void unbind() {
    // ThreadLocalMap.remove(TXC_CONTEXT_KEY);
    // ThreadLocalMap.remove(TXC_CONTEXT_MANAGER);
    String xid = getCurrentXid();
    if (xid != null) {
      REDO_CONTEXT_MAP.remove(xid);
    }
    DtsContextOperateHelper.removeUserData(TXC_XID_KEY);
    DtsContextOperateHelper.removeUserData(TXC_XID_OWNER_KEY);
    DtsContextOperateHelper.removeUserData(TXC_NEXT_SVR_ADDR);
  }

  public static String suspendTxcTransaction() {
    return (String) DtsContextOperateHelper.removeUserData(TXC_XID_KEY);
  }

  public static void resumeTxcTransaction(String xid) {
    if (xid != null)
      DtsContextOperateHelper.putUserData(TXC_XID_KEY, xid);
  }

  public static void startRetryBranch(long effectiveTime) {
    DtsContextOperateHelper.putUserData(TXC_RETRY_BRANCH_KEY, Long.toString(effectiveTime));
  }

  public static void endRetryBranch() {
    DtsContextOperateHelper.removeUserData(TXC_RETRY_BRANCH_KEY);
  }

  public static boolean inRetryContext() {
    return DtsContextOperateHelper.getUserData(TXC_RETRY_BRANCH_KEY) != null;
  }

  public static long getEffectiveTime() {
    String s = DtsContextOperateHelper.getUserData(TXC_RETRY_BRANCH_KEY);
    if (s != null) {
      return Long.parseLong(s);
    } else
      return -1;
  }

  public static void putMtUdata(String mtUdata) {
    if (mtUdata != null)
      DtsContextOperateHelper.putUserData(MT_UDATA_KEY, mtUdata);
  }

  public static String removeMtUdata() {
    return (String) DtsContextOperateHelper.removeUserData(MT_UDATA_KEY);
  }

  public static boolean inTxcEnv() {
    return DtsContext.inTxcTransaction() || DtsContext.inRetryContext();
  }
}
