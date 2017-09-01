package org.dts.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by guoyubo on 2017/9/1.
 */
@Component
@ConfigurationProperties("app")
public class AppProperties {

  private String name;
  private String group;
  private int port;
  private String zkAddress;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(final String group) {
    this.group = group;
  }

  public int getPort() {
    return port;
  }

  public void setPort(final int port) {
    this.port = port;
  }

  public String getZkAddress() {
    return zkAddress;
  }

  public void setZkAddress(final String zkAddress) {
    this.zkAddress = zkAddress;
  }
}
