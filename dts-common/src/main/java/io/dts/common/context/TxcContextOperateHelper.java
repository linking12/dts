package io.dts.common.context;

/**
 * 对于TDDL3的情况，由于分库分表后，由一条线程来串行执行SQL，不存在上下文传递<br>
 * 
 * @author songshu.zss
 * 
 */
public class TxcContextOperateHelper {


	private static ITxcContextOperate txcContext = null;


	public static String getUserData(String key) {
		return txcContext.getUserData(key);
	}

	public static String putUserData(String key, String value) {

		return txcContext.putUserData(key, value);
	}

	public static String removeUserData(String key) {


		return txcContext.removeUserData(key);
	}

	public static void main(String[] args) {
		TxcContextOperateHelper.putUserData("aaa", "vaaa");
		TxcContextOperateHelper.removeUserData("aaa");
	}
}
