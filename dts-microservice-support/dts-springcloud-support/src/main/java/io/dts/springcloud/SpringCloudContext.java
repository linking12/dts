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

import java.util.HashMap;
import java.util.Map;

import io.dts.common.context.DtsContext;

/**
 * @author liushiming
 * @version SpringCloudContext.java, v 0.0.1 2017年11月20日 下午1:50:22 liushiming
 */
public class SpringCloudContext extends DtsContext {
  private static final ThreadLocal<SpringCloudContext> LOCAL =
      new InheritableThreadLocal<SpringCloudContext>() {

        @Override
        protected SpringCloudContext initialValue() {
          return new SpringCloudContext();
        }
      };
  private final Map<String, String> attachments = new HashMap<String, String>();


  public static SpringCloudContext getContext() {
    return LOCAL.get();
  }

  public SpringCloudContext setAttachment(String key, String value) {
    if (value == null) {
      attachments.remove(key);
    } else {
      attachments.put(key, value);
    }
    return this;
  }

  public String getAttachment(String key) {
    return attachments.get(key);
  }

  public SpringCloudContext removeAttachment(String key) {
    attachments.remove(key);
    return this;
  }

  public Map<String, String> getAttachments() {
    return attachments;
  }

  @Override
  public String getCurrentXid() {
    return SpringCloudContext.getContext().getAttachment(TXC_XID_KEY);
  }

  @Override
  public void bind(String xid) {
    SpringCloudContext.getContext().setAttachment(TXC_XID_KEY, xid);
  }

  @Override
  public void unbind() {
    SpringCloudContext.getContext().removeAttachment(TXC_XID_KEY);
  }

  @Override
  public boolean inTxcTransaction() {
    return getCurrentXid() != null;
  }

  @Override
  public int priority() {
    return 0;
  }
}
