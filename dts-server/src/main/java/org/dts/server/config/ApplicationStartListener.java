package org.dts.server.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Created by guoyubo on 2017/9/1.
 */
@Configurable
public class ApplicationStartListener implements ApplicationListener {


  @Override
  public void onApplicationEvent(final ApplicationEvent applicationEvent) {
  }
}
