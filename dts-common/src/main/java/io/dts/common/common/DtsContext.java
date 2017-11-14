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

import com.quancheng.saluki.core.common.RpcContext;

import io.dts.common.exception.DtsException;

/**
 * @author liushiming
 * @version DtsContext2Helper.java, v 0.0.1 2017年10月27日 下午3:24:34 liushiming
 */
public class DtsContext {

  private static final String TXC_XID_KEY = "XID";
  private static final String TXC_NEXT_SVR_ADDR = "NEXT_SVR_ADDR";

  public static String getTxcNextSvrAddr() {
    return RpcContext.getContext().getAttachment(TXC_NEXT_SVR_ADDR);
  }

  public static String getCurrentXid() {
    return RpcContext.getContext().getAttachment(TXC_XID_KEY);
  }

  public static void bind(String xid, String nextSvrAddr) throws DtsException {
    RpcContext.getContext().setAttachment(TXC_XID_KEY, xid);
    if (nextSvrAddr != null)
      RpcContext.getContext().setAttachment(TXC_NEXT_SVR_ADDR, nextSvrAddr);
  }

  public static void unbind() {
    RpcContext.getContext().removeAttachment(TXC_XID_KEY);
    RpcContext.getContext().removeAttachment(TXC_NEXT_SVR_ADDR);
  }

  public static boolean inTxcTransaction() {
    return getCurrentXid() != null;
  }


}
