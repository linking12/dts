package io.dts.common.context;

public interface IDtsContextOperate {
	public <T> T getUserData(String key);

	public <T> T putUserData(String key, T value);

	public <T> T removeUserData(String key);
}
