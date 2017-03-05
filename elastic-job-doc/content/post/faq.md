+++
date = "2016-01-27T16:14:21+08:00"
title = "FAQ"
weight=400
+++

# FAQ

### 1. 阅读源码时为什么会出现编译错误?

回答：

`Elastic-Job`使用`lombok`实现极简代码。关于更多使用和安装细节，请参考[lombok官网](https://projectlombok.org/download.html)。

***

### 2. 使用`Spring`命名空间时在网上相应地址找不到`xsd`?

回答：

`Spring`命名空间使用规范并未强制要求将`xsd`文件部署至公网地址，只需在`jar`包的`META-INF\spring.schemas`配置，并在`jar`包中相关位置存在即可。

我们并未将`http://www.dangdang.com/schema/ddframe/reg/reg.xsd`和`http://www.dangdang.com/schema/ddframe/job/job.xsd`部署至公网，但并不影响使用。相关问题请参考`Spring`命名空间规范。

***

### 3. 为什么在代码或`Spring`配置文件中修改了作业配置，注册中心配置却没有更新?

回答：

`Elastic-Job-Lite`采用无中心化设计，若每个客户端的配置不一致，不做控制的话，最后一个启动的客户端配置将会成为注册中心的最终配置。

`Elastic-Job-Lite`提出了`overwrite`概念，可通过`JobConfiguration`或`Spring`命名空间配置。`overwrite=true`即允许客户端配置覆盖注册中心，反之则不允许。如果注册中心无相关作业的配置，则无论`overwrite`是否配置，客户端配置都将写入注册中心。

***

### 4. 怀疑`Elastic-Job-Lite`在分布式环境中有问题，但无法重现又不能在线上环境调试，应该怎么做?

回答：

分布式问题非常难于调试和重现，为此`Elastic-Job-Lite`提供了`dump`命令。

如果您怀疑某些场景出现问题，可参照[dump文档](../user_guide/other/lite_dump/)将作业运行时信息发给我们、提交`issue`或发至`QQ`群讨论。我们已将`IP`等敏感信息过滤，`dump`出来的信息可在公网安全传输。

***

### 5. `Elastic-Job`有何使用限制?

回答：

`Elastic-Job-Lite`和`Elastic-Job-Cloud`的共同使用限制：

* 作业启动成功后修改作业名称视为新作业，原作业废弃。

`Elastic-Job-Lite`的使用限制：

* 同一台作业服务器只能运行一个相同的作业实例，因为作业运行时是按照`IP`注册和管理的。

* 一旦有服务器波动，或者修改分片项，将会触发重新分片；触发重新分片将会导致运行中的流式处理的作业在执行完本次作业后不再继续执行，等待分片结束后再恢复正常。

* 开启`monitorExecution`才能实现分布式作业幂等性（即不会在多个作业服务器运行同一个分片）的功能，但`monitorExecution`对短时间内执行的作业（如每5秒一触发）性能影响较大，建议关闭并自行实现幂等性。

***

### 6. 是否支持动态添加作业?

回答：

动态添加作业这个概念每个人理解不尽相同。

`elastic-job-lite`为`jar`包，由开发或运维人员负责启动。启动时自动向注册中心注册作业信息并进行分布式协调，因此并不需要手工在注册中心填写作业信息。
但注册中心与作业部署机无从属关系，注册中心并不能控制将单点的作业分发至其他作业机，也无法将远程服务器未启动的作业启动。`elastic-job-lite`并不会包含`ssh`免密管理等功能。

`elastic-job-cloud`为`mesos`框架，由`mesos`负责作业启动和分发。
但需要将作业打包上传，并调用`elastic-job-cloud`提供的`REST API`写入注册中心。
打包上传属于部署系统的范畴`elastic-job-cloud`并未涉及。

综上所述，`elastic-job`已做了基本动态添加功能，但无法做到真正意义的完全自动化添加。

***

### 7. 使用`Spring`版本有何限制?

回答：

`Elastic-Job`的`Spring`版本支持从`3.1.0.RELEASE`至`4`的任何版本。`Spring 5`由于仅支持`JDK 8`及其以上版本，因此目前并不支持。`Spring 3.1.0`之前的版本对占位符的使用与目前不同，因此不再支持。`Elastic-Job`并未包含`Spring`的`maven`依赖，请自行添加您需要的版本。

***

### 8. `Zookeeper`版本不是`3.4.6`会有什么问题?

回答：

根据测试，使用`3.3.6`版本的`Zookeeper`在使用`Curator 2.10.0`的`CuratorTransactionFinal`的`commit`时会导致死锁。

***

### 9. 作业暂停(`Pause`)与作业失效(`Disable`)的区别是什么?

回答：

作业暂停和失效都会停止当前节点作业的运行。但作业暂停和恢复不会触发重分片，而作业失效和生效将触发重分片。

***

### 10. `Elastic-Job 2.0.0`版本`API`改动较大，升级时需要注意哪些问题?

回答：

基于扩展性提升，概念明晰和命名规范化的考虑，`elastic-job 2.0.0`版本决定抛弃原有包袱的束缚，重新定义了`JAVA API`，`Spring`命名空间并且删除了已废弃的`API`。

**重新定义`JAVA API`**

* 配置分为`Core`, `Type`和`Root`3个层级，使用类装饰者模式创建。

* 作业从继承抽象类改为接口化，提供`SimpleJob`, `DataflowJob`和`ScriptJob`接口。

* `DataflowJob`作业类型简化，去除批量和逐条处理分别，统一使用批量处理，`THROUGHPUT`和`SEQUENCE`作业不再提供单独接口，而是统一通过配置方式实现。

**重新定义`Spring`命名空间**

* 删除`<job:bean>`，细化为`<job:simple>`, `<job:dataflow>`, `<job:script>`具体类型。

* `Spring`命名空间属性由驼峰式修正为`Spring`命名空间标准命名规范(多单词以`-`分隔)。

* 作业的`Spring`命名空间属性`regCenter`变更为`registry-center-ref`。

**删除非核心功能**

* 删除`offset`功能。

* 删除n分钟内正确错误数量统计功能，未来由更加全面的作业事件追踪功能替换。

* 删除内嵌的`Zookeeper`注册中心，改为在`example`启动时开启内嵌注册中心，而非在`Elastic-Job`的核心代码中开启。

**废弃过时`API`**

* 删除废弃作业类，包括`AbstractOneOffElasticJob`，`AbstractPerpetualElasticJob`和`AbstractSequencePerpetualElasticJob`。

* 删除废弃作业调度器类，包括`com.dangdang.ddframe.job.schedule.JobController`和`com.dangdang.ddframe.job.spring.schedule.SpringJobController`。

* 不再支持非`Spring`命名空间通过`xml`方式配置`bean`，如有需要请使用`Spring Java Config`。

### 11. `Elastic-Job 2.0.5`版本使用Cloud需要注意哪些问题?

回答：

对于Elastic Job Cloud，原作业维度配置无法满足易用性和扩展性等需求，因此在`elastic-job 2.0.5`Cloud版本中增加了`作业APP`的概念，即作业打包部署后的应用，描述了作业启动需要用到的CPU、内存、启动脚本及应用下载路径等基本信息，每个APP可以包含一个或多个作业。

**增加`JOB APP API`**

* 将作业打包部署后发布作业APP。

* 作业APP配置参数cpuCount,memoryMB分别代表应用启动时需要用到的`CPU`及内存。

**调整`JOB API`**

* 新增作业时，必须先发布打包部署后的作业APP。

* 作业配置参数cpuCount,memoryMB分别代表作业运行时需要用到的`CPU`及内存。