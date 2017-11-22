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

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;

/**
 * @author liushiming
 * @version HystrixConcurrencyStrategy.java, v 0.0.1 2017年11月22日 下午5:58:46 liushiming
 */
public class ContextHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

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
