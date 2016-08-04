+++
date = "2016-06-07T16:14:21+08:00"
title = "1.x elastic-job 1.1.0升级说明"
weight=1002
+++

# elastic-job 1.1.0升级说明

* 基于扩展性提升，概念明晰和命名规范化的考虑，`elastic-job 1.1.0`版本决定抛弃原有包袱的束缚，重新定义了`JAVA API`，`Spring`命名空间并且删除了已废弃的`API`。

* `elastic-job 1.1.0`作为里程碑版本发布，除了`API`改动并未做功能上的修改，希望通过标准化配置的方式为未来的新`elastic-job`功能的开发打下良好的基础。

## 重新定义JAVA API

* 变更`JobConfiguration`类为接口，通过工厂方法配合构建者模式为每种作业类型分别提供了配置实现类。

* 取消`DataFlowElasticJob`接口中`isStreamingProcess`的方法签名，归入`DataFlow`型作业配置，默认值为`false`非流式处理。

## 重新定义Spring命名空间

* 删除`<job:bean>`，细化为`<job:simple>`, `<job:dataflow>`, `<job:script>`具体类型。

* `Spring`命名空间属性由驼峰式修正为`Spring`命名空间标准命名规范(多单词以`-`分隔)。

* 作业的`Spring`命名空间属性`regCenter`变更为`registry-center-ref`。

## 废弃过时API

* 删除废弃作业类，包括`AbstractOneOffElasticJob`，`AbstractPerpetualElasticJob`和`AbstractSequencePerpetualElasticJob`。

* 删除废弃作业调度器类，包括`com.dangdang.ddframe.job.schedule.JobController`和`com.dangdang.ddframe.job.spring.schedule.SpringJobController`。

* 不再支持非`Spring`命名空间通过`xml`方式配置`bean`，如有需要请使用`Spring Java Config`。