package com.quancheng.dts.message.response;

import com.quancheng.dts.RemotingSerializable;

/**
 * Created by guoyubo on 2017/8/30.
 */
public class TransactionCommitBody extends RemotingSerializable {

  private String xid;

  private String nextServerAddr;

  public String getXid() {
    return xid;
  }

  public void setXid(final String xid) {
    this.xid = xid;
  }

  public String getNextServerAddr() {
    return nextServerAddr;
  }

  public void setNextServerAddr(final String nextServerAddr) {
    this.nextServerAddr = nextServerAddr;
  }
}
