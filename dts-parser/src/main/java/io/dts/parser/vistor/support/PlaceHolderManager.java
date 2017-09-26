package io.dts.parser.vistor.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dts.common.exception.DtsException;

/*
 * 非线程安全
 */
public class PlaceHolderManager {
	/**
	 * 保存了对占位数据的处理
	 */
	private final Map<Integer, List<Object>> placeHolderMap = new HashMap<Integer, List<Object>>();

	public void addToMap(int place, Object value) {
		List<Object> list = null;
		if (placeHolderMap.containsKey(place)) {
			list = placeHolderMap.get(place);
		} else {
			list = new ArrayList<Object>();
			placeHolderMap.put(place, list);
		}

		list.add(value);
	}

	public List<Object> getPlaceHolder(int index) {
		if (placeHolderMap.containsKey(index)) {
			return placeHolderMap.get(index);
		}
		throw new DtsException("PlaceHolderMap has no key[" + index + "]");
	}

	/**
	 * SQL中含有几个问号
	 * 
	 * @return
	 */
	public int getPlaceHolderNum() {
		return placeHolderMap.size();
	}

	/**
	 * 批量处理的SQL有多少条
	 * 
	 * @return
	 */
	public int getPlaceHolderLineNum() {
		if (getPlaceHolderNum() > 0) {
			return getPlaceHolder(1).size();
		}
		return 0;
	}
}
