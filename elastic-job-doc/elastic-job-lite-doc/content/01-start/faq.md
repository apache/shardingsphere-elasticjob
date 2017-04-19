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

Elastic-Job使用lombok实现极简代码。关于更多使用和安装细节，请参考[lombok官网](https://projectlombok.org/download.html)。

***

### 2. 使用Spring命名空间时在网上相应地址找不到xsd?

回答：

Spring命名空间使用规范并未强制要求将xsd文件部署至公网地址，因此我们并未将`http://www.dangdang.com/schema/ddframe/reg/reg.xsd`和`http://www.dangdang.com/schema/ddframe/job/job.xsd`部署至公网，但不影响正常使用。

elastic-job-lite-spring的jar包中`META-INF\spring.schemas`配置了xsd文件的位置：http\://www.dangdang.com/schema/ddframe/reg/reg.xsd=META-INF/namespace/reg.xsd http\://www.dangdang.com/schema/ddframe/job/job.xsd=META-INF/namespace/job.xsd，需确保jar包中该文件存在。

***

### 3. 为什么在代码或Spring配置文件中修改了作业配置，注册中心配置却没有更新?

回答：

Elastic-Job-Lite采用无中心化设计，若每个客户端的配置不一致，不做控制的话，最后一个启动的客户端配置将会成为注册中心的最终配置。

Elastic-Job-Lite提出了overwrite概念，可通过JobConfiguration或Spring命名空间配置。overwrite=true即允许客户端配置覆盖注册中心，反之则不允许。如果注册中心无相关作业的配置，则无论overwrite是否配置，客户端配置都将写入注册中心。

***

### 4. 怀疑Elastic-Job-Lite在分布式环境中有问题，但无法重现又不能在线上环境调试，应该怎么做?

回答：

分布式问题非常难于调试和重现，为此Elastic-Job-Lite提供了dump命令。

如果您怀疑某些场景出现问题，可参照[dump文档](/02-guide/dump/)将作业运行时信息发给我们、提交issue或发至QQ群讨论。我们已将IP等敏感信息过滤，dump出来的信息可在公网安全传输。

***

### 5. Elastic-Job有何使用限制?

回答：

* 作业启动成功后修改作业名称视为新作业，原作业废弃。

* 同一台作业服务器可以运行多个相同的作业实例，但每个作业实例必须使用不同的JobInstanceId，因为作业运行时是按照IP和JobInstanceId注册和管理的。JobInstanceId可在作业配置中设置。

* 一旦有服务器波动，或者修改分片项，将会触发重新分片；触发重新分片将会导致运行中的流式处理的作业在执行完本次作业后不再继续执行，等待分片结束后再恢复正常。

* 开启monitorExecution才能实现分布式作业幂等性（即不会在多个作业服务器运行同一个分片）的功能，但monitorExecution对短时间内执行的作业（如每5秒一触发）性能影响较大，建议关闭并自行实现幂等性。

***

### 6. 是否支持动态添加作业?

回答：

动态添加作业这个概念每个人理解不尽相同。

elastic-job-lite为jar包，由开发或运维人员负责启动。启动时自动向注册中心注册作业信息并进行分布式协调，因此并不需要手工在注册中心填写作业信息。
但注册中心与作业部署机无从属关系，注册中心并不能控制将单点的作业分发至其他作业机，也无法将远程服务器未启动的作业启动。elastic-job-lite并不会包含ssh免密管理等功能。

elastic-job-cloud为mesos框架，由mesos负责作业启动和分发。
但需要将作业打包上传，并调用elastic-job-cloud提供的REST API写入注册中心。
打包上传属于部署系统的范畴elastic-job-cloud并未涉及。

综上所述，elastic-job已做了基本动态添加功能，但无法做到真正意义的完全自动化添加。

***

### 7. 使用Spring版本有何限制?

回答：

Elastic-Job的Spring版本支持从3.1.0.RELEASE至4的任何版本。Spring 5由于仅支持JDK 8及其以上版本，因此目前并不支持。Spring 3.1.0之前的版本对占位符的使用与目前不同，因此不再支持。Elastic-Job并未包含Spring的maven依赖，请自行添加您需要的版本。

***

### 8. Zookeeper版本不是3.4.6会有什么问题?

回答：

根据测试，使用3.3.6版本的Zookeeper在使用Curator 2.10.0的CuratorTransactionFinal的commit时会导致死锁。

***

### 9. Elastic-Job 2.0.5及之前版本的作业暂停(Pause)与作业失效(Disable)的区别是什么?

回答：

作业暂停和失效都会停止当前节点作业的运行。但作业暂停和恢复不会触发重分片，而作业失效和生效将触发重分片。

***

### 10. Elastic-Job 2.0.0版本API改动较大，升级时需要注意哪些问题?

回答：

基于扩展性提升，概念明晰和命名规范化的考虑，elastic-job 2.0.0版本决定抛弃原有包袱的束缚，重新定义了JAVA API，Spring命名空间并且删除了已废弃的API。

**重新定义JAVA API**

* 配置分为Core, Type和Root3个层级，使用类装饰者模式创建。

* 作业从继承抽象类改为接口化，提供SimpleJob, DataflowJob和ScriptJob接口。

* DataflowJob作业类型简化，去除批量和逐条处理分别，统一使用批量处理，THROUGHPUT和SEQUENCE作业不再提供单独接口，而是统一通过配置方式实现。

**重新定义Spring命名空间**

* 删除`<job:bean>`，细化为`<job:simple>, <job:dataflow>, <job:script>`具体类型。

* Spring命名空间属性由驼峰式修正为Spring命名空间标准命名规范(多单词以-分隔)。

* 作业的Spring命名空间属性regCenter变更为registry-center-ref。

**删除非核心功能**

* 删除offset功能。

* 删除n分钟内正确错误数量统计功能，未来由更加全面的作业事件追踪功能替换。

* 删除内嵌的Zookeeper注册中心，改为在example启动时开启内嵌注册中心，而非在Elastic-Job的核心代码中开启。

**废弃过时API**

* 删除废弃作业类，包括AbstractOneOffElasticJob，AbstractPerpetualElasticJob和AbstractSequencePerpetualElasticJob。

* 删除废弃作业调度器类，包括com.dangdang.ddframe.job.schedule.JobController和com.dangdang.ddframe.job.spring.schedule.SpringJobController。

* 不再支持非Spring命名空间通过xml方式配置bean，如有需要请使用Spring Java Config。

### 11. Elastic-Job 2.1.0版本支持单节点运行多个相同的作业实例，是否兼容原来的数据结构?

回答：

是的。新Elastic-Job Lite的数据结构和原有结构完全兼容。

### 12. 界面Console无法正常显示?

回答：

使用Web Console时应确保与Elastic-Job相关jar包版本保持一致，否则会导致不可用。
