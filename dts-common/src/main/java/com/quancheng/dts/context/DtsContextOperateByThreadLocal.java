package com.quancheng.dts.context;

import java.util.HashMap;
import java.util.Map;

import com.quancheng.dts.util.serviceloader.LoadLevel;

@LoadLevel(name = "TxcContextOperateByThreadLocal", order = 1)
public class DtsContextOperateByThreadLocal implements IDtsContextOperate {
  private final static ThreadLocal<Map<Object, Object>> threadContext = new MapThreadLocal();

  @Override
  public String getUserData(String key) {
    return (String) getContextMap().get(key);
  }

  @Override
  public String putUserData(String key, String value) {
    return (String) getContextMap().put(key, value);
  }

  @Override
  public String removeUserData(String key) {
    return (String) getContextMap().remove(key);
  }

  private static class MapThreadLocal extends ThreadLocal<Map<Object, Object>> {
    @Override
    protected Map<Object, Object> initialValue() {
      return new HashMap<Object, Object>() {
        private static final long serialVersionUID = -8252491460479785489L;

        public Object put(Object key, Object value) {
          return super.put(key, value);
        }
      };
    }
  }

  /**
   * 取得thread context Map的实例。
   * 
   * @return thread context Map的实例
   */
  public static Map<Object, Object> getContextMap() {
    return (Map<Object, Object>) threadContext.get();
  }
}
