package io.dts.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;

import java.beans.PropertyVetoException;

/**
 * Created by guoyubo on 2017/7/30.
 */
@Configuration
public class DataSourceConfig  implements EnvironmentAware {

  @Autowired
  public Environment env;

  @Autowired
  public DataSourceProperties dataSourceProperties;


  @Bean(name="dataSource")
  public DataSource dataSource() throws PropertyVetoException {
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
    dataSource.setUrl(dataSourceProperties.getUrl());
    dataSource.setUsername(dataSourceProperties.getUsername());
    dataSource.setPassword(dataSourceProperties.getPassword());
    dataSource.setMaxWait(dataSourceProperties.getMaxWait());
    dataSource.setMaxActive(dataSourceProperties.getMaxActive());
    return dataSource;
  }

  @Bean(name = "transactionManager")
  public PlatformTransactionManager annotationDrivenTransactionManager(@Autowired DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean(name = "transactionTemplate")
  public TransactionTemplate transactionTemplate(@Autowired PlatformTransactionManager transactionManager) {
    return new TransactionTemplate(transactionManager);
  }

  @Override
  public void setEnvironment(final Environment environment) {
    this.env = environment;
  }
}
