package com.quancheng.saluki.example.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiService;
import com.quancheng.saluki.example.repository.StudentDao;
import com.quancheng.saluki.example.repository.entity.StudentDo;

@SalukiService
public class HelloServiceImpl implements HelloService {

  @Autowired
  private StudentDao studentDao;

  @Override
  @Transactional
  public HelloReply dtsNormal(HelloRequest hellorequest) {
    StudentDo studentDo = new StudentDo();
    studentDo.setName("liushiming");
    studentDao.save(studentDo);
    HelloReply reply = new HelloReply();
    reply.setMessage("update");
    return reply;
  }

  @Override
  public HelloReply dtsException(HelloRequest hellorequest) {
    throw new RuntimeException("rollback");
    // HelloReply reply = new HelloReply();
    // reply.setMessage("none");
    // return reply;
  }



}
