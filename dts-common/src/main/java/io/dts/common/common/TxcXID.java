package io.dts.common.common;

/**
 * TXC的全局事务ID
 * 
 * @author jiangyu.jy
 * 
 */
public class TxcXID {
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
		TxcXID.port = port;
	}

	/**
	 * @param ipAddress
	 */
	public static void setIpAddress(String ipAddress) {
		TxcXID.ipAddress = ipAddress;
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
		if (TxcXID.svrAddr == null) {
			TxcXID.svrAddr = TxcXID.ipAddress + ":" + TxcXID.port;
		}
		return TxcXID.svrAddr;
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

	/**
	 * 判断XID是否合法
	 * 
	 * @param xid
	 * @return
	 */
	public static boolean isValidXid(String xid) {
		boolean f = true;
		try {
			TxcXID.getTransactionId(xid);
		} catch (Exception e) {
			f = false;
		}

		return f;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(TxcXID.getServerAddress("localhost:8080:21123132"));
	}
}
