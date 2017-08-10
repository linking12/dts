package com.quancheng.dts.context;

public interface IDtsContextOperate {
	public String getUserData(String key);

	public String putUserData(String key, String value);

	public String removeUserData(String key);
}
