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
package io.dts.springcloud;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * @author liushiming
 * @version SpringCloudHystrixAutoConfiguration.java, v 0.0.1 2017年11月22日 下午6:10:02 liushiming
 */
@Configuration
@ConditionalOnClass(HystrixCommand.class)
@ConditionalOnProperty(value = "spring.sleuth.hystrix.strategy.enabled", matchIfMissing = true)
public class ContextHystrixAutoConfiguration {
  @Bean
  ContextHystrixConcurrencyStrategy contextHystrixConcurrencyStrategy() {
    return new ContextHystrixConcurrencyStrategy();
  }
}
