+++
date = "2016-01-27T16:14:21+08:00"
title = "FAQ"
weight=40
+++

# FAQ

### 1. 阅读源码时为什么会出现编译错误?

回答：

我们使用`lombok`实现极简代码。关于更多使用和安装细节，请参考[lombok官网](https://projectlombok.org/download.html)。

***

### 2. 使用`Spring`命名空间时在网上相应地址找不到`xsd`?

回答：

`Spring`命名空间使用规范并未强制要求将`xsd`文件部署至公网地址，只需在`jar`包的`META-INF\spring.schemas`配置，并在`jar`包中相关位置存在即可。

我们并未将`http://www.dangdang.com/schema/ddframe/reg/reg.xsd`和`http://www.dangdang.com/schema/ddframe/job/job.xsd`部署至公网，但并不影响使用。相关问题请参考`Spring`命名空间规范。

***

### 3. 怀疑`Elastic-Job`在分布式环境中有问题，但无法重现又不能在线上环境调试，应该怎么做?

回答：

分布式问题非常难于调试和重现，为此`Elastic-Job`提供了`dump`命令。

如果您怀疑某些场景出现问题，可参照[dump文档](../user_guide/other/lite_dump/)将作业运行时信息发给我们、提交`issue`或发至`QQ`群讨论。我们已将`IP`等敏感信息过滤，`dump`出来的信息可在公网安全传输。

***

### 4. `Elastic-Job`有何使用限制?

回答：

`Elastic-Job-Lite`和`Elastic-Job-Cloud`的共同使用限制：

* 作业启动成功后修改作业名称视为新作业，原作业废弃。

`Elastic-Job-Lite`的使用限制：

* 同一台作业服务器只能运行一个相同的作业实例，因为作业运行时是按照`IP`注册和管理的。

* 一旦有服务器波动，或者修改分片项，将会触发重新分片；触发重新分片将会导致运行中的流式处理的作业在执行完本次作业后不再继续执行，等待分片结束后再恢复正常。

* 开启`monitorExecution`才能实现分布式作业幂等性（即不会在多个作业服务器运行同一个分片）的功能，但`monitorExecution`对短时间内执行的作业（如每5秒一触发）性能影响较大，建议关闭并自行实现幂等性。

***

### 5. `Elastic-Job 1.1.0`版本`API`改动较大，升级时需要注意哪些问题?

回答：

基于扩展性提升，概念明晰和命名规范化的考虑，`elastic-job 1.1.0`版本决定抛弃原有包袱的束缚，重新定义了`JAVA API`，`Spring`命名空间并且删除了已废弃的`API`。
`elastic-job 1.1.0`作为里程碑版本发布，除了`API`改动并未做功能上的修改，希望通过标准化配置的方式为未来的新`elastic-job`功能的开发打下良好的基础。

**重新定义`JAVA API`**

* 变更`JobConfiguration`类为接口，通过工厂方法配合构建者模式为每种作业类型分别提供了配置实现类。

* 取消`DataFlowElasticJob`接口中`isStreamingProcess`的方法签名，归入`DataFlow`型作业配置，默认值为`false`非流式处理。

**重新定义`Spring`命名空间**

* 删除`<job:bean>`，细化为`<job:simple>`, `<job:dataflow>`, `<job:script>`具体类型。

* `Spring`命名空间属性由驼峰式修正为`Spring`命名空间标准命名规范(多单词以`-`分隔)。

* 作业的`Spring`命名空间属性`regCenter`变更为`registry-center-ref`。

**废弃过时`API`**

* 删除废弃作业类，包括`AbstractOneOffElasticJob`，`AbstractPerpetualElasticJob`和`AbstractSequencePerpetualElasticJob`。

* 删除废弃作业调度器类，包括`com.dangdang.ddframe.job.schedule.JobController`和`com.dangdang.ddframe.job.spring.schedule.SpringJobController`。

* 不再支持非`Spring`命名空间通过`xml`方式配置`bean`，如有需要请使用`Spring Java Config`。
