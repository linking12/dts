package org.dts.client.annotation;

import org.dts.client.DtsTransactionManager;
import org.dts.client.interceptor.DtsTransactionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * Created by guoyubo on 2017/9/5.
 */
@Configurable
public class DtsTransactionManagementConfiguration implements ImportAware {


  protected AnnotationAttributes enableTx;

  @Override
  public void setImportMetadata(AnnotationMetadata importMetadata) {
    this.enableTx = AnnotationAttributes.fromMap(
        importMetadata.getAnnotationAttributes(EnableDtsTransactionManagement.class.getName(), false));
    if (this.enableTx == null) {
      throw new IllegalArgumentException(
          "@EnableTransactionManagement is not present on importing class " + importMetadata.getClassName());
    }
  }


  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public DtsTransactionInterceptor transactionInterceptor(@Autowired DtsTransactionManager txManager) {
    DtsTransactionInterceptor interceptor = new DtsTransactionInterceptor();
    interceptor.setTransactionAttributeSource(transactionAttributeSource());
    if (txManager != null) {
      interceptor.setTransactionManager(txManager);
    }
    return interceptor;
  }

  @Bean(name = "org.dts.client.support.internalTransactionAdvisor")
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(@Autowired  DtsTransactionInterceptor transactionInterceptor) {
    BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
    advisor.setTransactionAttributeSource(transactionAttributeSource());
    advisor.setAdvice(transactionInterceptor);
    if (this.enableTx != null) {
      advisor.setOrder(this.enableTx.<Integer>getNumber("order"));
    }
    return advisor;
  }

  private TransactionAttributeSource transactionAttributeSource() {
    return new AnnotationDtsTransactionAttributeSource();
  }


}
