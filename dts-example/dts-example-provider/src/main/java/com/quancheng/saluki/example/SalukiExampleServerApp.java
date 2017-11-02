package com.quancheng.saluki.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SalukiExampleServerApp {

  public static void main(String[] args) {

    SpringApplication.run(SalukiExampleServerApp.class, args);
  }

}
