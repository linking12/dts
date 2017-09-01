package org.dts.server.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.ContextLoaderListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanFactoryUtil {

  private static boolean springInitialized = false;
  private static BeanFactory beanFactory;

  public static void afterSpringInited(BeanFactory beanFactory) {
    springInitialized = true;
    BeanFactoryUtil.beanFactory = beanFactory;
  }

  public static boolean isSpringInited() {
    return springInitialized;
  }

  public static BeanFactory getApplicationContext() {
    return beanFactory != null ? beanFactory : ContextLoaderListener.getCurrentWebApplicationContext();
  }

  public static Object getService(String name) {
    Object bean =  null;

    try {
      bean = beanFactory != null ? beanFactory.getBean(name) : getApplicationContext().getBean(name);
    } catch (BeansException e) {
      log.error("Failed to retrieve the bean named '{}' from application context!", name, e);
    }

    return bean;
  }

  public static <T> T getService(Class<T> requiredType) {
    BeanFactory ctx = getApplicationContext();
    return ctx != null ? ctx.getBean(requiredType) : null;
  }

  
}
