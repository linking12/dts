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

import java.util.Map;
import java.util.concurrent.Callable;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;

/**
 * @author liushiming
 * @version HystrixConcurrencyStrategy.java, v 0.0.1 2017年11月22日 下午5:58:46 liushiming
 */
public class ContextHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {


  public ContextHystrixConcurrencyStrategy() {
    HystrixCommandExecutionHook commandExecutionHook =
        HystrixPlugins.getInstance().getCommandExecutionHook();
    HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
    HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();
    HystrixPropertiesStrategy propertiesStrategy =
        HystrixPlugins.getInstance().getPropertiesStrategy();
    HystrixPlugins.reset();
    HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
    HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);
    HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
    HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
    HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
  }

  @Override
  public <T> Callable<T> wrapCallable(Callable<T> callable) {
    return new HystrixContextCallable<T>(callable);
  }

  static class HystrixContextCallable<S> implements Callable<S> {

    private final Callable<S> callable;
    private final Map<String, String> attachments;

    public HystrixContextCallable(Callable<S> callable) {
      this.attachments = SpringCloudContext.getContext().getAttachments();
      this.callable = callable;
    }

    @Override
    public S call() throws Exception {
      SpringCloudContext.getContext().setAttachment(attachments);
      return callable.call();
    }
  }

}
