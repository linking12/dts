package org.dts.client.remoting;

import com.quancheng.dts.exception.DtsException;
import com.quancheng.dts.message.response.TransactionBeginBody;
import com.quancheng.dts.rpc.cluster.AddressManager;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

/**
 * Created by guoyubo on 2017/8/24.
 */
public interface DtsClient {

  void start();

  void shutdown();

  void setTimeoutMillis(long timeoutMillis);

  <T> T invokeSync(RemotingCommand request, Long timeoutMillis, Class<T> classOfT) throws DtsException;

  <T> void invokeAsync(RemotingCommand request, Long timeoutMillis, Class<T> classOfT, DtsInvokeCallBack<T> dtsInvokeCallBack) throws DtsException;

  void setAddressManager(AddressManager addressManager);

  void setGroup(String group);

  void setAppName(String appName);
}
