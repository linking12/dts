package com.quancheng.saluki.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiReference;

@SpringBootApplication
public class SalukiExampleClientApp {


  public static void main(String[] args) {
    SpringApplication.run(SalukiExampleClientApp.class, args);
  }



}
