package com.quancheng.saluki.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.dts.client.aop.DtsTransactionScaner;
import io.dts.client.aop.annotation.EnableDtsConfiguration;

@SpringBootApplication
@EnableDtsConfiguration
public class SalukiExampleClientApp implements CommandLineRunner {

  @Autowired
  private DtsTransactionScaner scaner;

  public static void main(String[] args) {
    SpringApplication.run(SalukiExampleClientApp.class, args);
  }

  @Override
  public void run(String... arg0) throws Exception {
    System.out.println(scaner);
  }



}
