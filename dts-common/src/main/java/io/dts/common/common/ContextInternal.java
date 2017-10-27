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
package io.dts.common.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liushiming
 * @version DtsContext2.java, v 0.0.1 2017年10月27日 下午3:07:37 liushiming
 */
public class ContextInternal {
  private static final ThreadLocal<ContextInternal> LOCAL = new InheritableThreadLocal<ContextInternal>() {

    @Override
    protected ContextInternal initialValue() {
      return new ContextInternal();
    }
  };

  private final Map<String, String> attachments = new HashMap<String, String>();
  private final Map<String, Object> values = new HashMap<String, Object>();

  public static ContextInternal getContext() {
    return LOCAL.get();
  }

  public static void removeContext() {
    LOCAL.remove();
  }

  private ContextInternal() {}


  public String getAttachment(String key) {
    return attachments.get(key);
  }

  public ContextInternal setAttachment(String key, String value) {
    if (value == null) {
      attachments.remove(key);
    } else {
      attachments.put(key, value);
    }
    return this;
  }

  public void removeAttachment(String key) {
    attachments.remove(key);
  }

  public ContextInternal set(String key, Object value) {
    if (value == null) {
      values.remove(key);
    } else {
      values.put(key, value);
    }
    return this;
  }

  public Object get(String key) {
    return values.get(key);
  }

  public void remote(String key) {
    values.remove(key);
  }

  public void clear() {
    this.attachments.clear();
    this.values.clear();
  }



}
