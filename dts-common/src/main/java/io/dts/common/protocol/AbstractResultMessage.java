package io.dts.common.protocol;

import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;

/**
 * @author jiangyu.jy
 * 
 *         抽象结果消息
 */
public abstract class AbstractResultMessage implements DtsMessage {

	/**
	 * 0:失败; 1:成功;
	 */
	int result;

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	private String msg;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
