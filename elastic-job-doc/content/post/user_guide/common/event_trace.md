+++
date = "2016-09-27T16:14:21+08:00"
title = "Elastic-Job事件追踪"
weight=30
+++

# Elastic-Job事件追踪

`Elastic-Job`的`Lite`版和`Cloud`版都提供了事件追踪功能，可通过事件订阅的方式处理调度过程的重要事件，用于查询、统计和监控。`Elastic-Job`目前提供了基于日志和关系型数据库两种事件订阅方式记录事件。

## 基于日志的事件追踪

默认开启，通过应用程序配置的`logback.xml`或`log4j.xml`等日志框架文件输出事件日志。

## 基于关系型数据库的事件追踪

通过配置开启。具体配置方式请分别参见[Elastic-Job-Lite开发指南](../../lite/dev_guide/)和[Elastic-Job-Cloud开发指南](../../cloud/dev_guide/)。

将于作业配置的`JobEventConfiguration`的`url`属性对应库自动创建`JOB_EXECUTION_LOG`和`JOB_TRACE_LOG`两张表。

`JOB_EXECUTION_LOG`字段含义

| 字段名称           | 字段类型     | 是否必填 | 描述                                                                                             |
| ----------------- |:------------|:--------|:------------------------------------------------------------------------------------------------|
|id                 |VARCHAR(40)  |`是`     | 主键                                                                                             |
|job_name           |VARCHAR(100) |`是`     | 作业名称                                                                                          |
|hostname           |VARCHAR(255) |`是`     | 主机名称                                                                                          |
|sharding_item      |INT          |`是`     | 分片项                                                                                            |
|execution_source   |VARCHAR(20)  |`是`     | 作业执行来源。可选值为`NORMAL_TRIGGER`, `MISFIRE`, `FAILOVER`                                       |
|is_success         |BIT          |`是`     | 是否执行成功                                                                                       |
|failure_cause      |VARCHAR(2000)|否       | 执行失败原因                                                                                       |
|start_time         |TIMESTAMP    |`是`     | 作业开始执行时间                                                                                   |
|complete_time      |TIMESTAMP    |否       | 作业结束执行时间                                                                                   |


`JOB_EXECUTION_LOG`记录每次作业的执行历史。分为两个步骤：

1. 作业开始执行时向数据库插入数据，除`failure_cause`和`complete_time`外的其他字段均不为空。

2. 作业完成执行时向数据库更新数据，更新`is_success`, `complete_time`和`failure_cause`(如果作业执行失败)。

***

`JOB_TRACE_LOG`字段含义

| 字段名称           | 字段类型     | 是否必填 | 描述                                                                                             |
| ----------------- |:------------|:--------|:------------------------------------------------------------------------------------------------|
|id                 |VARCHAR(40)  |`是`     | 主键                                                                                             |
|job_name           |VARCHAR(100) |`是`     | 作业名称                                                                                          |
|hostname           |VARCHAR(255) |`是`     | 主机名称                                                                                          |
|log_level          |CHAR(5)      |`是`     | 日志级别。可选值为`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`                                        |
|message            |VARCHAR(2000)|`是`     | 相关信息                                                                                          |
|failure_cause      |VARCHAR(2000)|否       | 执行失败原因                                                                                       |
|creation_time      |TIMESTAMP    |`是`     | 记录创建时间                                                                                       |

`JOB_TRACE_LOG`记录作业运行痕迹表，可作为运行日志查看，原理同log框架，只能写入比配置日志级别更高的数据。
