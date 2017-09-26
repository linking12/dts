package io.dts.parser.hint;

import java.util.HashMap;
import java.util.Map;

import io.dts.parser.util.StringUtils;

// 'LEVEL':'READCOMMITED'
// 'LEVEL':'READUNCOMMITED'
// 'RULES':{'TYPE':'SECKILL','CONTENT':'ACCOUNT=ACCOUNT+3'}
public class TxcHint {
	public static String insulateLevel = "'" + "level" + "'";
	public static String readuncommited = "'" + "readuncommited" + "'";
	public static String readcommited = "'" + "readcommited" + "'";

	public static Map hint2map(String hintString) {
		String hint = null;
		while ((hint = relpaceStok(hintString)) != null) {
			hintString = hint;
		}

		return hint2map0(hintString, ",");
	}

	/**
	 * is declared with readcommited hint?
	 * 
	 * @param sql
	 * @return
	 */
	public static boolean isReadCommited(String sql) {
		return TxcHint.readcommited.equals(TxcHint.getInsulate(sql));
	}

	public static String getInsulate(String sql) {
		Map<String, Object> map = hint2map(getTxcHintString(sql));
		if (map == null) {
			return null;
		} else {
			return (String) map.get(insulateLevel);
		}
	}

	public static String getTxcHintString(String sql) {
		return StringUtils.getBetween(sql.toLowerCase(), "/*+txc({", "})*/");
	}

	public static String getTxcRule(String sql) {
		return StringUtils.getBetween(sql.toLowerCase(), "/*+txcrule(", ")*/");
	}

	public static String removeTxcHintString(String sql) {
		String tddlHint = getTxcHintString(sql);
		if (null == tddlHint || "".equals(tddlHint)) {
			return sql;
		}

		return StringUtils.removeBetweenWithSplitor(sql.toLowerCase(), "/*+txc({", "})*/");
	}

	public static String buildTxcHint(String hint) {
		return "/*+TXC({" + hint + "})*/";
	}

	// 替换掉所有{} -> []，并把{}中的逗号换成^
	private static String relpaceStok(String hintString) {
		int index1 = hintString.indexOf('{');
		int index2 = hintString.indexOf('}');

		if (index1 == -1) {
			return null;
		}

		char[] bytes = hintString.toCharArray();
		for (int i = index1; i < index2; i++) {
			if (bytes[i] == ',') {
				bytes[i] = '^';
			}
		}

		bytes[index1] = '[';
		bytes[index2] = ']';

		return String.valueOf(bytes);
	}

	private static Map hint2map0(String hintString, String regex) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] piece = hintString.split(regex);
		for (String s : piece) {
			int index = s.indexOf(':');
			if (index == -1) {
				continue;
			}

			String key = s.substring(0, index);
			String value = s.substring(index + 1, s.length());
			char[] valueOfCharArray = value.toCharArray();
			if (valueOfCharArray[0] == '[' && valueOfCharArray[value.length() - 1] == ']') {
				map.put(key, hint2map0(String.copyValueOf(valueOfCharArray, 1, value.length() - 2), "\\^"));
			} else {
				map.put(key, value);
			}
		}

		return map;
	}

	public static void main(String[] args) {
		if (true) {
			String hint0 = "'RULES':{'TYPE':'SECKILL','CONTENT':'ACCOUNT=ACCOUNT+3'}";
			String sql = buildTxcHint(hint0) + "select * from student";
			System.out.println(removeTxcHintString(sql));
			String hint = getTxcHintString(sql);
			System.out.println(hint);
			System.out.println(hint2map(hint));
		}

		System.out.println("-----------------------------");
		if (true) {
			String hint0 = "'LEVEL':'READUNCOMMITED'";
			String sql = buildTxcHint(hint0) + "select * from student";
			System.out.println(removeTxcHintString(sql));
			String hint = getTxcHintString(sql);
			System.out.println(hint);
			System.out.println(hint2map(hint));
			System.out.println(isReadCommited(sql));
		}

		System.out.println("-----------------------------");
		if (true) {
			String hint0 = "'LEVEL':'READCOMMITED'";
			String sql = buildTxcHint(hint0) + "select * from student";
			System.out.println(removeTxcHintString(sql));
			String hint = getTxcHintString(sql);
			System.out.println(hint);
			System.out.println(hint2map(hint));
			System.out.println(isReadCommited(sql));
		}
	}
}
