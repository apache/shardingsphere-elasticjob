+++
toc = true
date = "2016-01-27T16:14:21+08:00"
title = "FAQ"
weight = 2
prev = "/01-start/quick-start/"
next = "/01-start/dev-guide/"
+++

### 1. 阅读源码时为什么会出现编译错误?

回答：

参见[阅读源码编译问题说明](/00-overview/source-code-guide)

***

### 2. 使用Spring命名空间时在网上相应地址找不到xsd?

回答：

Spring命名空间使用规范并未强制要求将xsd文件部署至公网地址，因此我们并未将`http://www.dangdang.com/schema/ddframe/reg/reg.xsd和http://www.dangdang.com/schema/ddframe/job/job.xsd`部署至公网，但不影响正常使用。

elastic-job-lite-spring的jar包中META-INF\spring.schemas配置了xsd文件的位置：`http\://www.dangdang.com/schema/ddframe/reg/reg.xsd=META-INF/namespace/reg.xsd http\://www.dangdang.com/schema/ddframe/job/job.xsd=META-INF/namespace/job.xsd`，需确保jar包中该文件存在。

***

### 3. 为什么在代码或Spring配置文件中修改了作业配置，注册中心配置却没有更新?

回答：

Elastic-Job-Lite采用无中心化设计，若每个客户端的配置不一致，不做控制的话，最后一个启动的客户端配置将会成为注册中心的最终配置。

Elastic-Job-Lite提出了overwrite概念，可通过JobConfiguration或Spring命名空间配置。overwrite=true即允许客户端配置覆盖注册中心，反之则不允许。如果注册中心无相关作业的配置，则无论overwrite是否配置，客户端配置都将写入注册中心。

***

### 4. 怀疑Elastic-Job在分布式环境中有问题，但无法重现又不能在线上环境调试，应该怎么做?

回答：

分布式问题非常难于调试和重现，为此Elastic-Job提供了dump命令。

如果您怀疑某些场景出现问题，可参照[dump文档](/02-guide/dump/)将作业运行时信息发给我们、提交issue或发至QQ群讨论。我们已将IP等敏感信息过滤，dump出来的信息可在公网安全传输。

***

### 5. Elastic-Job有何使用限制?

回答：

* 作业一旦启动成功后不能修改作业名称，如果修改名称则视为新的作业。

* 同一台作业服务器只能运行一个相同的作业实例，因为作业运行时是按照IP注册和管理的。

* 一旦有服务器波动，或者修改分片项，将会触发重新分片；触发重新分片将会导致运行中的流式处理的作业在执行完本次作业后不再继续执行，等待分片结束后再恢复正常。

* 开启monitorExecution才能实现分布式作业幂等性（即不会在多个作业服务器运行同一个分片）的功能，但monitorExecution对短时间内执行的作业（如每5秒一触发）性能影响较大，建议关闭并自行实现幂等性。

***

### 6. 是否支持动态添加作业?

回答：

动态添加作业这个概念每个人理解不尽相同。

elastic-job为jar包，由开发或运维人员负责启动。启动时自动向注册中心注册作业信息并进行分布式协调，因此并不需要手工在注册中心填写作业信息。
但注册中心与作业部署机无从属关系，注册中心并不能控制将单点的作业分发至其他作业机，也无法将远程服务器未启动的作业启动。elastic-job并不会包含ssh免密管理等功能。

综上所述，elastic-job已做了基本动态添加功能，但无法做到真正意义的完全自动化添加。

***

### 7. Zookeeper版本不是3.4.6会有什么问题?

回答：

根据测试，使用3.3.6版本的Zookeeper在使用Curator 2.10.0的CuratorTransactionFinal的commit时会导致死锁。
