package com.quancheng.saluki.example.client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiReference;

import io.dts.client.aop.annotation.DtsTransaction;

@RestController
@RequestMapping("/proxy")
public class ProxyServiceController {


  @SalukiReference
  private HelloService helloService;


  @RequestMapping("/hello")
  public HelloReply hello() {
    return callService();
  }

  @DtsTransaction
  private HelloReply callService() {
    HelloRequest request = new HelloRequest();
    request.setName("liushiming");
    HelloReply reply = helloService.dtsNormal(request);
    helloService.dtsException(request);
    return reply;
  }


}
