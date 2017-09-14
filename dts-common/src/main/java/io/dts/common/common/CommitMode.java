package io.dts.common.common;

/**
 * 提交模式，即事务分支在一阶段还是二阶段提交
 * 
 * @author jiangyu.jy
 * 
 */
public enum CommitMode {
	/**
	 * 1阶段中做本地事务commit；AT模式
	 */
	COMMIT_IN_PHASE1(1),

	/**
	 * 2阶段中做本地事务commit；MT模式
	 */
	COMMIT_IN_PHASE2(2),

	/**
	 * 可重试SQL模式的提交方式；RT模式
	 */
	COMMIT_RETRY_MODE(3);

	private CommitMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	private int value;
}
