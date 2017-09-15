package io.dts.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * Created by guoyubo on 2017/9/15.
 */
@Configurable
public class AppConfig {

  public static int mId;

  @Autowired
  public Environment env;

  @PostConstruct
  public void init() {
    AppConfig.mId = Integer.parseInt(env.getProperty("mid"));
  }

}
