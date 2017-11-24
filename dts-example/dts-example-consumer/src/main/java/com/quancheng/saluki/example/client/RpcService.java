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
package com.quancheng.saluki.example.client;

import org.springframework.stereotype.Service;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.common.RpcContext;

import io.dts.client.aop.annotation.DtsTransaction;

/**
 * @author liushiming
 * @version RpcService.java, v 0.0.1 2017年11月7日 下午1:38:13 liushiming
 */
@Service
public class RpcService {

  @SalukiReference
  private HelloService helloService;



  @DtsTransaction
  public HelloReply callService() {
    RpcContext.getContext().set("123", "123");
    HelloRequest request = new HelloRequest();
    request.setName("liushiming");
    HelloReply reply = helloService.dtsNormal(request);
    helloService.dtsException(request);
    System.out.println(RpcContext.getContext().get("123"));
    return reply;
  }

}
