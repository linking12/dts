package org.dts.server.config;

import org.dts.server.processor.ProcessorFactory;
import org.dts.server.processor.TransactionBeginProcessor;
import org.dts.server.processor.TransactionCommitProcessor;
import org.dts.server.service.DefaultKeyGenerator;
import org.dts.server.service.DtsServer;
import org.dts.server.service.KeyGenerator;
import org.dts.server.utils.BeanFactoryUtil;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.quancheng.dts.RequestCode;

import javax.annotation.PostConstruct;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * Created by guoyubo on 2017/7/31.
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {

  @Autowired
  private AppProperties appProperties;

  @Autowired
  private BeanFactory beanFactory;

  @Bean("dtsServer")
  public DtsServer dtsServer() {
    DtsServer dtsServer = new DtsServer(appProperties.getGroup(), appProperties.getPort(), appProperties.getZkAddress());
    return dtsServer;
  }

  @PostConstruct
  public void init() {
    BeanFactoryUtil.afterSpringInited(beanFactory);
  }

  @Bean("processorFactory")
  public ProcessorFactory buildProcessorFactory(
      @Autowired TransactionBeginProcessor transactionBeginProcessor,
      @Autowired TransactionCommitProcessor transactionCommitProcessor) {
    ProcessorFactory processorFactory = new ProcessorFactory();
    processorFactory.setProcessor(RequestCode.TRANSACTION_BEGIN, transactionBeginProcessor);
    processorFactory.setProcessor(RequestCode.TRANSACTION_COMMIT, transactionCommitProcessor);
    return processorFactory;
  }

  @Bean("keyGenerator")
  public KeyGenerator defaultKeyGenerator() {
    DefaultKeyGenerator defaultKeyGenerator = new DefaultKeyGenerator();
    String workerId = System.getProperty("workerId");
    defaultKeyGenerator.setWorkerId(workerId == null ? 0  : Long.parseLong(workerId));
    return defaultKeyGenerator;
  }

  public final int getProcessID() {
    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    System.out.println(runtimeMXBean.getName());
    return Integer.valueOf(runtimeMXBean.getName().split("@")[0])
                  .intValue();
  }
}
