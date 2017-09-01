package com.quancheng.dts.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.quancheng.dts.util.serviceloader.EnhancedServiceLoader;

import java.util.List;

/**
 * 对于TDDL3的情况，由于分库分表后，由一条线程来串行执行SQL，不存在上下文传递<br>
 * 
 * @author songshu.zss
 * 
 */
public class DtsContextOperateHelper {
  private static final Logger logger = LoggerFactory.getLogger(DtsContextOperateHelper.class);
  private static IDtsContextOperate txcContext = null;

  static {
    txcContext = EnhancedServiceLoader.load(IDtsContextOperate.class);
    logger.info("EnhancedServiceLoader load txcContext engine:" + txcContext.getClass());
  }

  public static <T> T getUserData(String key) {
    return txcContext.getUserData(key);
  }

  public static <T> T putUserData(String key, T value) {
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("[Thread:%d] set Thread Context [%s:%s]",
          Thread.currentThread().getId(), key, value));
    }

    return txcContext.putUserData(key, value);
  }

  public static <T> void addUserData(String key, T value) {
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("[Thread:%d] set Thread Context [%s:%s]",
          Thread.currentThread().getId(), key, value));
    }
    List<T> userData = txcContext.getUserData(key);

    if (userData == null)  {
      txcContext.putUserData(key, Lists.newArrayList(value));
    } else {
      userData.add(value);
    }
  }

  public static String removeUserData(String key) {
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("[Thread:%d] del Thread Context [%s]",
          Thread.currentThread().getId(), key));
    }

    return txcContext.removeUserData(key);
  }


}
