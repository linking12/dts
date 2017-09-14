package io.dts.client.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;


import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

import io.dts.client.support.DtsATTransactionTemplate;
import io.dts.client.support.DtsTransactionCallback;
import io.dts.client.DtsTransactionManager;
import io.dts.client.support.DtsTransactionOperations;

/**
 * Created by guoyubo on 2017/9/4.
 */
public class DtsTransactionInterceptor implements BeanFactoryAware, MethodInterceptor, Serializable {

  private DtsTransactionManager transactionManager;

  private TransactionAttributeSource transactionAttributeSource;

  private BeanFactory beanFactory;


  private final ConcurrentMap<Object, DtsTransactionManager> transactionManagerCache =
      new ConcurrentReferenceHashMap<Object, DtsTransactionManager>(4);

  private String transactionManagerBeanName;

  private static final Object DEFAULT_TRANSACTION_MANAGER_KEY = new Object();


  public void setTransactionManager(final DtsTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public DtsTransactionManager getTransactionManager() {
    return transactionManager;
  }

  /**
   * Specify the name of the default transaction manager bean.
   */
  public void setTransactionManagerBeanName(String transactionManagerBeanName) {
    this.transactionManagerBeanName = transactionManagerBeanName;
  }

  /**
   * Return the name of the default transaction manager bean.
   */
  protected final String getTransactionManagerBeanName() {
    return this.transactionManagerBeanName;
  }


  @Override
  public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
    DtsTransactionAttribute transactionAttribute =
        (DtsTransactionAttribute) transactionAttributeSource.getTransactionAttribute(methodInvocation.getMethod(), methodInvocation.getClass());
    final DtsTransactionManager tx = determineTransactionManager(transactionAttribute);
    DtsTransactionOperations transactionOperations = null;

    transactionOperations =  new DtsATTransactionTemplate(tx);
    return transactionOperations.execute(new DtsTransactionCallback<Object>() {
      @Override
      public Object doInTransaction() throws Throwable {
        return methodInvocation.proceed();
      }
    }, transactionAttribute.getTimeout());
  }

  /**
   * Determine the specific transaction manager to use for the given transaction.
   */
  protected DtsTransactionManager determineTransactionManager(TransactionAttribute txAttr) {
    // Do not attempt to lookup tx manager if no tx attributes are set
    if (txAttr == null || this.beanFactory == null) {
      return getTransactionManager();
    }
    String qualifier = txAttr.getQualifier();
    if (StringUtils.hasText(qualifier)) {
      return determineQualifiedTransactionManager(qualifier);
    }
    else if (StringUtils.hasText(this.transactionManagerBeanName)) {
      return determineQualifiedTransactionManager(this.transactionManagerBeanName);
    }
    else {
      DtsTransactionManager defaultTransactionManager = getTransactionManager();
      if (defaultTransactionManager == null) {
        defaultTransactionManager = this.transactionManagerCache.get(DEFAULT_TRANSACTION_MANAGER_KEY);
        if (defaultTransactionManager == null) {
          defaultTransactionManager = this.beanFactory.getBean(DtsTransactionManager.class);
          this.transactionManagerCache.putIfAbsent(
              DEFAULT_TRANSACTION_MANAGER_KEY, defaultTransactionManager);
        }
      }
      return defaultTransactionManager;
    }
  }

  private DtsTransactionManager determineQualifiedTransactionManager(String qualifier) {
    DtsTransactionManager txManager = this.transactionManagerCache.get(qualifier);
    if (txManager == null) {
      txManager = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
          this.beanFactory, DtsTransactionManager.class, qualifier);
      this.transactionManagerCache.putIfAbsent(qualifier, txManager);
    }
    return txManager;
  }

  public void setTransactionAttributeSource(final TransactionAttributeSource transactionAttributeSource) {
    this.transactionAttributeSource = transactionAttributeSource;
  }

  @Override
  public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
