+++
date = "2016-01-27T16:14:21+08:00"
title = "elastic-job 1.0.2升级说明"
weight=2
+++

# elastic-job 1.0.2升级说明

为了扩展性和概念明晰，`elastic-job 1.0.2`重新定义了作业类型。

## plugin包

为了扩展方便，将作业类型由`api`包挪入`plugin`包，欢迎大家贡献自己的作业类型到`plugin`。

## 重新定义并明晰作业类型

* 原`OneOff`更名为`Simple`类型，意为简单实现，未经任何封装的类型。

* 提炼抽象出`DataFlow`类型用于处理数据流。

* 原`Perpetual`更名为`ThroughputDataFlow`类型，意为高吞吐的数据流作业。

* 原`SequencePerpetual`更名为`SequenceDataFlow`类型，意为顺序性数据流作业。

* `DataFlow`父接口增加`isStreamingProcess`接口，意为数据流类型作业是否使用流式处理（即，有数据则一直不停止作业的处理 -- 对应原`Perpetual`概念，还是任务中只执行一次抓取/处理 -- 对应原`OneOff`概念）。

## 迁移作业调度器类

* 迁移意义不明的`schedule`包，将其拆分至`api`和`internal`。

* `com.dangdang.ddframe.job.schedule.JobController`更名为`com.dangdang.ddframe.job.api.JobScheduler`，如果使用原生类启动作业，请更新。

* `com.dangdang.ddframe.job.spring.schedule.SpringJobController`更名为`com.dangdang.ddframe.job.spring.schedule.SpringJobScheduler`，如果使用非`Spring`命名空间启动作业，请更新。

## 兼容性问题

* 将原抽象类升级为接口，所以如果之前使用`protected`实现`fetchData`，`processData`等方法，需要改为`public`。

* 其余改动的接口，包括`AbstractOneOffElasticJob`，`AbstractPerpetualElasticJob`和`AbstractSequencePerpetualElasticJob`均将以`@Deprecated`的形式继续存在一段时间，不会导致升级版本产生兼容问题。
