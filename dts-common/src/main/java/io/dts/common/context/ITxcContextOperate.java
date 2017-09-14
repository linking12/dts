package io.dts.common.context;

public interface ITxcContextOperate {
	public String getUserData(String key);

	public String putUserData(String key, String value);

	public String removeUserData(String key);
}
