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

import com.quancheng.saluki.core.common.RpcContext;

import io.dts.common.context.DtsContext;

/**
 * @author liushiming
 * @version SalukiContext.java, v 0.0.1 2017年11月14日 下午6:17:41 liushiming
 */
public class SalukiContext extends DtsContext {

  @Override
  public String getCurrentXid() {
    return RpcContext.getContext().getAttachment(TXC_XID_KEY);
  }

  @Override
  public void bind(String xid) {
    RpcContext.getContext().setAttachment(TXC_XID_KEY, xid);
  }

  @Override
  public void unbind() {
    RpcContext.getContext().removeAttachment(TXC_XID_KEY);
    RpcContext.getContext().removeAttachment(TXC_NEXT_SVR_ADDR);
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
