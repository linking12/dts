package io.dts.common.common.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dts.common.util.serviceloader.EnhancedServiceLoader;

/**
 * 对于TDDL3的情况，由于分库分表后，由一条线程来串行执行SQL，不存在上下文传递<br>
 * 
 * @author songshu.zss
 * 
 */
public class TxcContextOperateHelper {

	private static final Logger logger = LoggerFactory.getLogger(TxcContextOperateHelper.class);

	private static IDtsContextOperate txcContext = null;

	static {
		txcContext = EnhancedServiceLoader.load(IDtsContextOperate.class);
		logger.info("EnhancedServiceLoader load txcContext engine:" + txcContext.getClass());
	}

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
