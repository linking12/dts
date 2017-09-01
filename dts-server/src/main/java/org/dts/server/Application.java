package org.dts.server;

import static org.springframework.boot.SpringApplication.run;

import org.dts.server.config.ApplicationStartListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by guoyubo on 2017/9/1.
 */
@ComponentScan(basePackages ="org.dts.server")
@MapperScan(basePackages = "org.dts.server.mapper",
    sqlSessionFactoryRef = "sqlSessionFactory")
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    ConfigurableApplicationContext run = run(Application.class, args);
    run.addApplicationListener(new ApplicationStartListener());
  }


}
