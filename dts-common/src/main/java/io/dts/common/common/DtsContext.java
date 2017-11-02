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
  private static final String TXC_RETRY_BRANCH_KEY = "RETRY_BRANCH";
  private static final String COMMIT_COUNT = "COMMIT_COUNT";
  private static final String BEGIN_COUNT = "BEGIN_COUNT";
  private static final String TXC_NEXT_SVR_ADDR = "NEXT_SVR_ADDR";

  private static RpcContext context = RpcContext.getContext();


  public static final int getBeginCount() {
    String num = context.getAttachment(BEGIN_COUNT);
    return num == null ? 0 : Integer.parseInt(num);
  }

  public static final void setBegin(int beginCount) {
    context.setAttachment(BEGIN_COUNT, String.valueOf(beginCount));
  }

  public static final int getCommitCount() {
    String num = context.getAttachment(COMMIT_COUNT);
    return num == null ? 0 : Integer.parseInt(num);
  }

  public static final void setCommitCount(int commitCount) {
    context.setAttachment(COMMIT_COUNT, String.valueOf(commitCount));
  }

  public static String getTxcNextSvrAddr() {
    return context.getAttachment(TXC_NEXT_SVR_ADDR);
  }

  public static String getCurrentXid() {
    return context.getAttachment(TXC_XID_KEY);
  }

  public static void bind(String xid, String nextSvrAddr) throws DtsException {
    context.setAttachment(TXC_XID_KEY, xid);
    if (nextSvrAddr != null)
      context.setAttachment(TXC_NEXT_SVR_ADDR, nextSvrAddr);
  }

  public static void unbind() {
    context.removeAttachment(TXC_XID_KEY);
    context.removeAttachment(TXC_NEXT_SVR_ADDR);
  }

  public static boolean inTxcTransaction() {
    return getCurrentXid() != null;
  }

  public static void startRetryBranch(long effectiveTime) {
    context.setAttachment(TXC_RETRY_BRANCH_KEY, Long.toString(effectiveTime));
  }

  public static void endRetryBranch() {
    context.removeAttachment(TXC_RETRY_BRANCH_KEY);
  }

  public static boolean inRetryContext() {
    return context.getAttachment(TXC_RETRY_BRANCH_KEY) != null;
  }

  public static long getEffectiveTime() {
    String s = context.getAttachment(TXC_RETRY_BRANCH_KEY);
    if (s != null) {
      return Long.parseLong(s);
    } else
      return -1;
  }


}
