package com.quancheng.saluki.example.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.examples.model.hello.HelloReply;

@RestController
@RequestMapping("/proxy")
public class ProxyServiceController {


  @Autowired
  private RpcService rpcService;

  @RequestMapping("/hello")
  public HelloReply hello() {
    return rpcService.callService();
  }

}
