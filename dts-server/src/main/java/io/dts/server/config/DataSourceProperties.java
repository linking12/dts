package io.dts.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by guoyubo on 2017/7/30.
 */
@Component
@ConfigurationProperties("datasource")
public class DataSourceProperties {

  private String driverClassName;

  private String url;

  private String username;

  private String password;

  private Integer initialSize = 1;

  private Integer minIdle = 1;

  private Integer maxActive = 20;

  private Integer maxWait = 60000;

  private Integer timeBetweenEvictionRunsMillis = 60 * 1000;

  private Integer minEvictableIdleTimeMillis = 30 * 1000;

  private Boolean testOnBorrow = true;

  private Boolean testOnReturn = false;

  private String validationQuery = "SELECT 1";

  private Boolean testWhileIdle = true;

  private Boolean poolPreparedStatements = false;

  private Integer maxPoolPreparedStatementPerConnectionSize = -1;

  public String getDriverClassName() {
    return driverClassName;
  }

  public void setDriverClassName(final String driverClassName) {
    this.driverClassName = driverClassName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public Integer getInitialSize() {
    return initialSize;
  }

  public void setInitialSize(final Integer initialSize) {
    this.initialSize = initialSize;
  }

  public Integer getMinIdle() {
    return minIdle;
  }

  public void setMinIdle(final Integer minIdle) {
    this.minIdle = minIdle;
  }

  public Integer getMaxActive() {
    return maxActive;
  }

  public void setMaxActive(final Integer maxActive) {
    this.maxActive = maxActive;
  }

  public Integer getMaxWait() {
    return maxWait;
  }

  public void setMaxWait(final Integer maxWait) {
    this.maxWait = maxWait;
  }

  public Integer getTimeBetweenEvictionRunsMillis() {
    return timeBetweenEvictionRunsMillis;
  }

  public void setTimeBetweenEvictionRunsMillis(final Integer timeBetweenEvictionRunsMillis) {
    this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
  }

  public Integer getMinEvictableIdleTimeMillis() {
    return minEvictableIdleTimeMillis;
  }

  public void setMinEvictableIdleTimeMillis(final Integer minEvictableIdleTimeMillis) {
    this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
  }

  public Boolean getTestOnBorrow() {
    return testOnBorrow;
  }

  public void setTestOnBorrow(final Boolean testOnBorrow) {
    this.testOnBorrow = testOnBorrow;
  }

  public Boolean getTestOnReturn() {
    return testOnReturn;
  }

  public void setTestOnReturn(final Boolean testOnReturn) {
    this.testOnReturn = testOnReturn;
  }

  public String getValidationQuery() {
    return validationQuery;
  }

  public void setValidationQuery(final String validationQuery) {
    this.validationQuery = validationQuery;
  }

  public Boolean getTestWhileIdle() {
    return testWhileIdle;
  }

  public void setTestWhileIdle(final Boolean testWhileIdle) {
    this.testWhileIdle = testWhileIdle;
  }

  public Boolean getPoolPreparedStatements() {
    return poolPreparedStatements;
  }

  public void setPoolPreparedStatements(final Boolean poolPreparedStatements) {
    this.poolPreparedStatements = poolPreparedStatements;
  }

  public Integer getMaxPoolPreparedStatementPerConnectionSize() {
    return maxPoolPreparedStatementPerConnectionSize;
  }

  public void setMaxPoolPreparedStatementPerConnectionSize(final Integer maxPoolPreparedStatementPerConnectionSize) {
    this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
  }
}
