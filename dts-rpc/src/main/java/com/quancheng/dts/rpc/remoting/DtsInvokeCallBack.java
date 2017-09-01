package com.quancheng.dts.rpc.remoting;

/**
 * Created by guoyubo on 2017/8/31.
 */
public interface DtsInvokeCallBack<T> {

  void execute(T t);
}
