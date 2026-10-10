+++
title = "Java API"
weight = 1
chapter = true
+++

## 注册中心配置

用于注册和协调作业分布式行为的组件。
ElasticJob 支持 `ZooKeeper`（默认）、`etcd`、`Nacos` 和内存注册中心。
除 `elasticjob-bootstrap` 外，还需要添加注册中心实现对应的 Maven 依赖：

| 注册中心     | Maven 依赖 |
|----------|------|
| ZooKeeper | `org.apache.shardingsphere.elasticjob:elasticjob-registry-center-zookeeper-curator` |
| etcd     | `org.apache.shardingsphere.elasticjob:elasticjob-registry-center-etcd` |
| Nacos    | `org.apache.shardingsphere.elasticjob:elasticjob-registry-center-nacos` |
| Memory   | `org.apache.shardingsphere.elasticjob:elasticjob-registry-center-memory` |

依赖版本使用 `${elasticjob.version}`。
各注册中心实现的可配置属性与使用示例，请参阅[注册中心配置](/cn/user-manual/configuration/registry-center)。

默认的 ZooKeeper 实现如下所示。

类名称：org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration

可配置属性：

| 属性名                           | 构造器注入 |
|-------------------------------|:------|
| serverLists                   | 是     |
| namespace                     | 是     |
| baseSleepTimeMilliseconds     | 否     |
| maxSleepTimeMilliseconds      | 否     |
| maxRetries                    | 否     |
| sessionTimeoutMilliseconds    | 否     |
| connectionTimeoutMilliseconds | 否     |
| digest                        | 否     |

## 作业配置

类名称：org.apache.shardingsphere.elasticjob.api.JobConfiguration

可配置属性：

| 属性名                               | 构造器注入 |
|-----------------------------------|:------|
| jobName                           | 是     |
| shardingTotalCount                | 是     |
| cron                              | 否     |
| timeZone                          | 否     |
| shardingItemParameters            | 否     |
| jobParameter                      | 否     |
| monitorExecution                  | 否     |
| failover                          | 否     |
| misfire                           | 否     |
| maxTimeDiffSeconds                | 否     |
| reconcileIntervalMinutes          | 否     |
| jobShardingStrategyType           | 否     |
| jobExecutorThreadPoolSizeProvider | 否     |
| jobErrorHandlerType               | 否     |
| jobListenerTypes                  | 否     |
| description                       | 否     |
| props                             | 否     |
| disabled                          | 否     |
| overwrite                         | 否     |
