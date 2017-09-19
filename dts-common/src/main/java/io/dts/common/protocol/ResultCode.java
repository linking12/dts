package io.dts.common.protocol;

/**
 * @author jiangyu.jy
 * 
 *         result code
 */
public enum ResultCode {
	/**
	 * 成功
	 */
	OK(1),

	/**
	 * 失败，可恢复错误，需要重试
	 */
	SYSTEMERROR(0),

	/**
	 * 失败，逻辑错误，不可恢复
	 */
	LOGICERROR(2);

	private ResultCode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	private int value;
}
