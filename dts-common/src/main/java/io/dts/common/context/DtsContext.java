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
package io.dts.common.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author liushiming
 * @version DtsContext2Helper.java, v 0.0.1 2017年10月27日 下午3:24:34 liushiming
 */
public abstract class DtsContext {
  protected static final String TXC_XID_KEY = "XID";
  private static List<DtsContext> contexts;
  static {
    contexts = load();
  }

  private static List<DtsContext> load() {
    Iterable<DtsContext> candidates = ServiceLoader.load(DtsContext.class);
    List<DtsContext> list = new ArrayList<DtsContext>();
    for (DtsContext current : candidates) {
      list.add(current);
    }
    Collections.sort(list, Collections.reverseOrder(new Comparator<DtsContext>() {
      @Override
      public int compare(DtsContext f1, DtsContext f2) {
        return f1.priority() - f2.priority();
      }
    }));
    return Collections.unmodifiableList(list);
  }

  public static DtsContext getInstance() {
    if (contexts == null || contexts.isEmpty()) {
      contexts = load();
    }
    return contexts.get(0);
  }

  public abstract int priority();

  public abstract String getCurrentXid();

  public abstract void bind(String xid);

  public abstract void unbind();

  public abstract boolean inTxcTransaction();


}
