/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.dts.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import io.dts.server.remoting.DtsServerController;

/**
 * @author liushiming
 * @version Application.java, v 0.0.1 2017年9月5日 下午6:31:29 liushiming
 */
@SpringBootApplication
@EnableConfigurationProperties(DtsServerProperties.class)
public class DtsServerApplication {

  public static void main(String[] args) throws Exception {
    ConfigurableApplicationContext context =
        SpringApplication.run(DtsServerApplication.class, args);
    DtsServerController tcpServer = context.getBean(DtsServerController.class);
    tcpServer.start();
  }

}
