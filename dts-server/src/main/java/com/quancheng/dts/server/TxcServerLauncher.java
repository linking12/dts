package com.quancheng.dts.server;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TxcServerLauncher {
	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("txc-server-context.xml");
		System.in.read();
		context.close();
	}
}
