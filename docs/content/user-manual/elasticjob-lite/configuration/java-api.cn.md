+++
title = "Java API"
weight = 1
chapter = true
+++

## 注册中心配置

用于注册和协调作业分布式行为的组件，目前仅支持 ZooKeeper。

类名称：org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration

可配置属性：

| 属性名                         | 构造器注入 |
| ----------------------------- |:--------- |
| serverLists                   | 是        |
| namespace                     | 是        |
| baseSleepTimeMilliseconds     | 否        |
| maxSleepTimeMilliseconds      | 否        |
| maxRetries                    | 否        |
| sessionTimeoutMilliseconds    | 否        |
| connectionTimeoutMilliseconds | 否        |
| digest                        | 否        |

## 作业配置

类名称：org.apache.shardingsphere.elasticjob.api.JobConfiguration

可配置属性：

| 属性名                         | 构造器注入 |
| ----------------------------- |:--------- |
| jobName                       | 是        |
| shardingTotalCount            | 是        |
| cron                          | 否        |
| shardingItemParameters        | 否        |
| jobParameter                  | 否        |
| monitorExecution              | 否        |
| failover                      | 否        |
| misfire                       | 否        |
| maxTimeDiffSeconds            | 否        |
| reconcileIntervalMinutes      | 否        |
| jobShardingStrategyType       | 否        |
| jobExecutorServiceHandlerType | 否        |
| jobErrorHandlerType           | 否        |
| jobListenerTypes              | 否        |
| description                   | 否        |
| props                         | 否        |
| disabled                      | 否        |
| overwrite                     | 否        |
