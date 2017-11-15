# 概述

  Dts是一款高性能、高可靠、接入简单的分布式事务中间件,用于解决分布式环境下的事务一致性问题;<br/>
  在单机数据库下很容易维持事务的 ACID特性，但在分布式系统中并不容易，DTS可以保证分布式系统中的分布式事务的 ACID 特性

# 功能
* 跨消息和数据库的分布式事务 <br/>
  在某些业务场景中，需要进行多个 DB 操作的同时，还会调用消息系统，DB 操作成功、消息发送失败或者反过来都会造成业务的不完整
* 跨服务的分布式事务 <br/>
  业务完成服务化后，资源与客户端调用解耦，同时又要保证多个服务调用间资源的变化保持强一致，否则会造成业务数据的不完整,DTS支持跨服务的事务
  
# 详细说明

* Dts Server：事务协调器。负责分布式事务的推进，管理事务生命周期
* Dts Client：事务发起者。通过事务协调器，开启、提交、回滚分布式事务
* Dts Resource：资源，包括数据库、MQ

# 架构方案
  请查看根目录下的架构图

# Compile
```
   mvn install -Dmaven.test.skip=true
```
# 关于Sample
  详细请查看 <a href="https://github.com/linking12/dts/tree/master/dts-example">sample</a>
  
# Quick Start
* 客户端及资源端添加pom依赖

```
<dependency>
	<groupId>io.dts</groupId>
	<artifactId>dts-saluki-support</artifactId>
	<version>${dts.version}</version>
</dependency>
```

* 在Dts客户端、Dts资源端、Dts服务端的启动参数加上-DZK_CONNECTION=127.0.0.1:2181，zookeeper的连接地址，Dts使用zookeeper来做集群管理

* 客户端，在服务调用不同的接口添加@DtsTransaction注解，将两个服务调用纳入整个分布式事务管理


```
 @DtsTransaction
  public HelloReply callService() {
    HelloRequest request = new HelloRequest();
    request.setName("liushiming");
    HelloReply reply = helloService.dtsNormal(request);
    helloService.dtsException(request);
    return reply;
  }

```
* 资源端，针对数据库资源，使用Dts的适配DtsDataSource来使数据库连接池转变为Dts资源

1. 执行script/resource.sql脚本 
2. 将数据库连接池适配为Dts资源端 ,如下示例：

```
  @Bean
  @Primary
  public DataSource dataSource() {
    DruidDataSource datasource = new DruidDataSource();
    int startIndex = dbUrl.lastIndexOf("/");
    String databaseName = dbUrl.substring(startIndex + 1, dbUrl.length());
    datasource.setConnectionProperties(connectionProperties);
    return new DtsDataSource(datasource, databaseName);
  }

```

* 服务端，针对spring boot直接启动Main，将事务协调器启动起来

1. 执行script/server.sql脚本
2. 启动事务协调器服务端
