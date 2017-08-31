/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.dts.rpc.remoting;

import com.quancheng.dts.rpc.cluster.AddressManager;
import com.quancheng.dts.rpc.remoting.exception.RemotingConnectException;
import com.quancheng.dts.rpc.remoting.exception.RemotingSendRequestException;
import com.quancheng.dts.rpc.remoting.exception.RemotingTimeoutException;
import com.quancheng.dts.rpc.remoting.exception.RemotingTooMuchRequestException;
import com.quancheng.dts.rpc.remoting.netty.NettyRequestProcessor;
import com.quancheng.dts.rpc.remoting.protocol.RemotingCommand;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface RemotingClient extends RemotingService {

    public void updateNameServerAddressList(final List<String> addrs);

    public List<String> getNameServerAddressList();

    void setAddressManager(final AddressManager addressManager);

    void setGroup(final String group);

    public RemotingCommand invokeSync(final String addr, final RemotingCommand request,
        final long timeoutMillis) throws InterruptedException, RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException;

    public void invokeAsync(final String addr, final RemotingCommand request, final long timeoutMillis,
        final InvokeCallback invokeCallback) throws InterruptedException, RemotingConnectException,
        RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    public void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis)
        throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
        RemotingTimeoutException, RemotingSendRequestException;

    public void registerProcessor(final int requestCode, final NettyRequestProcessor processor,
        final ExecutorService executor);

    public boolean isChannelWriteable(final String addr);
}
