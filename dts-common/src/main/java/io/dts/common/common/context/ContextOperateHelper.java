package io.dts.common.common.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对于TDDL3的情况，由于分库分表后，由一条线程来串行执行SQL，不存在上下文传递<br>
 * 
 * @author songshu.zss
 * 
 */
public class ContextOperateHelper {

  private static final Logger logger = LoggerFactory.getLogger(ContextOperateHelper.class);

  private static IDtsContextOperate dtsContext = null;

  static {
    dtsContext = new DtsContextOperateThreadLocal();
    logger.info("EnhancedServiceLoader load dtsContext engine:" + dtsContext.getClass());
  }

  public static String getUserData(String key) {
    return dtsContext.getUserData(key);
  }

  public static String putUserData(String key, String value) {

    return dtsContext.putUserData(key, value);
  }

  public static String removeUserData(String key) {


    return dtsContext.removeUserData(key);
  }

}
