package io.dts.client.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;

import io.dts.client.aop.annotation.DtsTransaction;

public class DtsTransactionScaner extends AbstractAutoProxyCreator {
  private static final Logger logger = LoggerFactory.getLogger(DtsTransactionScaner.class);

  private static final long serialVersionUID = -8650586520064930251L;
  /**
   * 保存自己处理了的代理类，避免重复代理
   */
  private final static Set<String> proxyedSet = new HashSet<String>();

  private TransactionDtsInterceptor interceptor;

  public DtsTransactionScaner() {
    logger.info("txc trasaction scaner initing....");
  }

  @Override
  protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
    try {
      synchronized (proxyedSet) {
        if (proxyedSet.contains(beanName)) {
          return bean;
        }
        proxyedSet.add(beanName);
        Class<?> serviceInterface = findTargetClass(bean);
        Method[] methods = serviceInterface.getMethods();
        LinkedList<MethodDesc> methodDescList = new LinkedList<MethodDesc>();
        for (Method method : methods) {
          DtsTransaction anno = method.getAnnotation(DtsTransaction.class);
          if (anno != null) {
            methodDescList.add(makeMethodDesc(anno, method));
          }
        }
        if (methodDescList.size() != 0) {
          interceptor = new TransactionDtsInterceptor(methodDescList);
        } else {
          return bean;
        }
        if (!AopUtils.isAopProxy(bean)) {// 未被代理过
          bean = super.wrapIfNecessary(bean, beanName, cacheKey);
        } else {
          // 已代理则加入当前代理
          AdvisedSupport advised = getAdvisedSupport(bean);
          Advisor[] advisor =
              buildAdvisors(beanName, getAdvicesAndAdvisorsForBean(null, null, null));
          for (Advisor avr : advisor) {
            advised.addAdvisor(0, avr);
          }
        }
        return bean;// 返回被代理对象
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private MethodDesc makeMethodDesc(Object bean, Method m) {
    DtsTransaction anno = m.getAnnotation(DtsTransaction.class);
    return new MethodDesc(anno, m);
  }

  private Class<?> findTargetClass(Object proxy) throws Exception {
    if (AopUtils.isAopProxy(proxy)) {
      AdvisedSupport advised = getAdvisedSupport(proxy);
      Object target = advised.getTargetSource().getTarget();
      return findTargetClass(target);
    } else {
      return proxy.getClass();
    }
  }

  private AdvisedSupport getAdvisedSupport(Object proxy) throws Exception {
    Field h;
    if (AopUtils.isJdkDynamicProxy(proxy)) {
      h = proxy.getClass().getSuperclass().getDeclaredField("h");
    } else {
      h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
    }
    h.setAccessible(true);
    Object dynamicAdvisedInterceptor = h.get(proxy);
    Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
    advised.setAccessible(true);
    return (AdvisedSupport) advised.get(dynamicAdvisedInterceptor);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass, String beanName,
      TargetSource customTargetSource) throws BeansException {
    return new Object[] {interceptor};
  }
}
