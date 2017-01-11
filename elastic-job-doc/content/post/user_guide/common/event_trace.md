+++
date = "2016-09-27T16:14:21+08:00"
title = "Elastic-Job事件追踪"
weight=30
+++

# Elastic-Job事件追踪

`Elastic-Job`的`Lite`版和`Cloud`版都提供了事件追踪功能，可通过事件订阅的方式处理调度过程的重要事件，用于查询、统计和监控。`Elastic-Job`目前提供了基于关系型数据库两种事件订阅方式记录事件。

## 基于关系型数据库的事件追踪

通过配置开启。具体配置方式请分别参见[Elastic-Job-Lite开发指南](../../lite/dev_guide/)和[Elastic-Job-Cloud-Scheduler启动指南](../../cloud/scheduler_guide/)。

事件追踪的`event_trace_rdb_url`属性对应库自动创建`JOB_EXECUTION_LOG`和`JOB_STATUS_TRACE_LOG`两张表以及若干索引。

`JOB_EXECUTION_LOG`字段含义

| 字段名称           | 字段类型     | 是否必填 | 描述                                                                                             |
| ----------------- |:------------|:--------|:------------------------------------------------------------------------------------------------|
|id                 |VARCHAR(40)  |`是`     | 主键                                                                                             |
|job_name           |VARCHAR(100) |`是`     | 作业名称                                                                                          |
|task_id            |VARCHAR(1000)|`是`     | 任务名称,每次作业运行生成新任务                                                                      |
|hostname           |VARCHAR(255) |`是`     | 主机名称                                                                                          |
|ip                 |VARCHAR(50)  |`是`     | 主机IP                                                                                            |
|sharding_item      |INT          |`是`     | 分片项                                                                                            |
|execution_source   |VARCHAR(20)  |`是`     | 作业执行来源。可选值为`NORMAL_TRIGGER`, `MISFIRE`, `FAILOVER`                                       |
|failure_cause      |VARCHAR(2000)|否       | 执行失败原因                                                                                       |
|is_success         |BIT          |`是`     | 是否执行成功                                                                                       |
|start_time         |TIMESTAMP    |`是`     | 作业开始执行时间                                                                                   |
|complete_time      |TIMESTAMP    |否       | 作业结束执行时间                                                                                   |


`JOB_EXECUTION_LOG`记录每次作业的执行历史。分为两个步骤：

1. 作业开始执行时向数据库插入数据，除`failure_cause`和`complete_time`外的其他字段均不为空。

2. 作业完成执行时向数据库更新数据，更新`is_success`, `complete_time`和`failure_cause`(如果作业执行失败)。

***

`JOB_STATUS_TRACE_LOG`字段含义

| 字段名称           | 字段类型     | 是否必填 | 描述                                                                                                                       |
| ----------------- |:------------|:--------|:--------------------------------------------------------------------------------------------------------------------------|
|id                 |VARCHAR(40)  |`是`     | 主键                                                                                                                       |
|job_name           |VARCHAR(100) |`是`     | 作业名称                                                                                                                    |
|original_task_id   |VARCHAR(1000)|`是`     | 原任务名称                                                                                                                  |
|task_id            |VARCHAR(1000)|`是`     | 任务名称                                                                                                                    |
|slave_id           |VARCHAR(1000)|`是`     | 执行作业服务器的名称，`Lite`版本为服务器的`IP`地址，`Cloud`版本为`Mesos`执行机主键                                                 |
|source             |VARCHAR(50)  |`是`     | 任务执行源，可选值为`CLOUD_SCHEDULER`, `CLOUD_EXECUTOR`, `LITE_EXECUTOR`                                                      |
|execution_type     |VARCHAR(20)  |`是`     | 任务执行类型，可选值为`NORMAL_TRIGGER`, `MISFIRE`, `FAILOVER`                                                                 |
|sharding_item      |VARCHAR(255) |`是`     | 分片项集合，多个分片项以逗号分隔                                                                                                |
|state              |VARCHAR(20)  |`是`     | 任务执行状态，可选值为`TASK_STAGING`, `TASK_RUNNING`, `TASK_FINISHED`, `TASK_KILLED`, `TASK_LOST`, `TASK_FAILED`, `TASK_ERROR`|
|message            |VARCHAR(2000)|`是`     | 相关信息                                                                                                                    |
|creation_time      |TIMESTAMP    |`是`     | 记录创建时间                                                                                                                 |

`JOB_STATUS_TRACE_LOG`记录作业状态变更痕迹表。可通过每次作业运行的`task_id`查询作业状态变化的生命周期和运行轨迹。
