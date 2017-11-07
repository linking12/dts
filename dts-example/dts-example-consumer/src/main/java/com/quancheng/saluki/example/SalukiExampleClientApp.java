package com.quancheng.saluki.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"io.dts.client.aop", "com.quancheng.saluki.example.client"})
public class SalukiExampleClientApp {


  public static void main(String[] args) {
    SpringApplication.run(SalukiExampleClientApp.class, args);
  }



}
