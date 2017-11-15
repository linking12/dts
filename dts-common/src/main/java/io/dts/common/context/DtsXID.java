package io.dts.common.context;


public class DtsXID {
  /**
   * 服务端端口
   */
  private static int port;

  /**
   * 服务端IP
   */
  private static String ipAddress;

  /**
   * 服务端地址
   */
  private static String svrAddr;

  /**
   * @param port
   */
  public static void setPort(int port) {
    DtsXID.port = port;
  }

  /**
   * @param ipAddress
   */
  public static void setIpAddress(String ipAddress) {
    DtsXID.ipAddress = ipAddress;
  }

  public static String getBranchName(String xid, long branchId) {
    return xid + branchId;
  }

  public static long getGlobalXID(String xid, long branchID) {
    return branchID;
  }

  public synchronized static String generateXID(long tranId) {
    return ipAddress + ":" + port + ":" + tranId;
  }

  public static String getSvrAddr() {
    if (DtsXID.svrAddr == null) {
      DtsXID.svrAddr = DtsXID.ipAddress + ":" + DtsXID.port;
    }
    return DtsXID.svrAddr;
  }

  public static int getTransactionId(String xid) {
    if (xid == null) {
      return -1;
    }

    int idx = xid.lastIndexOf(":");
    return Integer.parseInt(xid.substring(idx + 1));
  }

  public static String getServerAddress(String xid) {
    if (xid == null) {
      return null;
    }
    int idx = xid.lastIndexOf(":");
    return xid.substring(0, idx);
  }

  public static String formatXid(long tranId, long branchId) {
    StringBuilder infoAppender = new StringBuilder();
    infoAppender.append("[");
    infoAppender.append(tranId);
    infoAppender.append(":");
    infoAppender.append(branchId);
    infoAppender.append("] ");
    return infoAppender.toString();
  }

  public static String formatXid(String xid, long branchId) {
    StringBuilder infoAppender = new StringBuilder();
    infoAppender.append(" [");
    infoAppender.append(getServerAddress(xid));
    infoAppender.append("] ");
    infoAppender.append(formatXid(getTransactionId(xid), branchId));
    return infoAppender.toString();
  }


  public static boolean isValidXid(String xid) {
    boolean f = true;
    try {
      DtsXID.getTransactionId(xid);
    } catch (Exception e) {
      f = false;
    }

    return f;
  }

}
