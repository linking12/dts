package com.quancheng.dts.message.response;

import com.quancheng.dts.RemotingSerializable;

import lombok.Data;

/**
 * Created by guoyubo on 2017/8/30.
 */
@Data
public class TransactionBeginBody extends RemotingSerializable {

  private String xid;

  private String nextServerAddr;

}
