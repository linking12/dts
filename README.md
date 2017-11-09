# 概述

* Dts的目标是让用户透明地使用分布式事务功能，把业务与事务完全分离
* 整个系统实现类似于Alibaba的GTS服务


# 系统组成

* 事务发起方Client
* 事务协调者Server
* 资源管理器Resource,包括(Datasource、RabbitMq、RocketMq)

# 系统架构
![flow](architecture.png)

# 事务流程
![flow](flow.png)