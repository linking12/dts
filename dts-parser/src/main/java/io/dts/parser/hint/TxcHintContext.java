package io.dts.parser.hint;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TxcHintContext {
	private boolean isFastFailed = false;
	private final Map hintMap;
	private final String hintString;

	private static long cacheSize = 1000;
	private static long expireTime = 300 * 1000;
	private final static Cache<String, TxcHintContext>
        cacheOfRulesContext = CacheBuilder.newBuilder().maximumSize(cacheSize).expireAfterWrite(expireTime, TimeUnit.MILLISECONDS).softValues().build();

	public static TxcHintContext getHintContext(final String hintString) {
		try {
			return cacheOfRulesContext.get(hintString, new Callable<TxcHintContext>() {
				@Override
				public TxcHintContext call() throws Exception {
					return new TxcHintContext(hintString);
				}
			});
		} catch (ExecutionException e) {
			return new TxcHintContext(hintString);
		}
	}

	public static boolean isSeckillHint(final String hintString) {
		boolean flag = false;
		TxcHintContext context = getHintContext(hintString);
		Map map = context.getHintMap();

		Map<String, String> rules = (Map) map.get("rules");
		for (Map.Entry<String, String> entry : rules.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if ("seckill".equals(key)) {
				flag = true;
				break;
			}
		}

		return flag;
	}

	public static boolean checkFailFastStatus(final String hintString) {
		TxcHintContext context = getHintContext(hintString);
		return context.isFastFailed();
	}

	public static void setFailFastStatus(final String hintString) {
		TxcHintContext context = getHintContext(hintString);
		context.setFastFailed(true);
	}

	private TxcHintContext(String hintString) {
		this.hintString = hintString;
		this.hintMap = TxcHint.hint2map(hintString);
	}

	public Map getHintMap() {
		return hintMap;
	}

	public boolean isFastFailed() {
		return isFastFailed;
	}

	public void setFastFailed(boolean isFastFailed) {
		this.isFastFailed = isFastFailed;
	}
}
