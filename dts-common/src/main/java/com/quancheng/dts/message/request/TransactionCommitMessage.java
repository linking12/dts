package com.quancheng.dts.message.request;

import com.quancheng.dts.RemotingSerializable;

/**
 * Created by guoyubo on 2017/8/30.
 */
public class TransactionCommitMessage extends RemotingSerializable {

  private long transId;
}
