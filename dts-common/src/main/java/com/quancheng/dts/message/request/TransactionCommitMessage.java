package com.quancheng.dts.message.request;

import com.quancheng.dts.RemotingSerializable;

import lombok.Data;

/**
 * Created by guoyubo on 2017/8/30.
 */
@Data
public class  TransactionCommitMessage extends RemotingSerializable {

  private long transId;

  private int retryTimes;

}
