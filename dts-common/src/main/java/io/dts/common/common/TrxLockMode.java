package io.dts.common.common;

public enum TrxLockMode {
	DELETE_TRX_LOCK(1), NOT_DELETE_TRX_LOCK(0);

	private TrxLockMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	private int value;
}
