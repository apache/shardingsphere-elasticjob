+++
title = "高可用"
weight = 2
chapter = true
+++

## 介绍

调度器的高可用是通过运行几个指向同一个 ZooKeeper 集群的 ElasticJob-Cloud-Scheduler 实例来实现的。
ZooKeeper 用于在当前主 ElasticJob-Cloud-Scheduler 实例失败的情况下执行领导者选举。
通过至少两个调度器实例来构成集群，集群中只有一个调度器实例提供服务，其他实例处于`待命`状态。
当该实例失败时，集群会选举剩余实例中的一个来继续提供服务。

## 配置

每个 ElasticJob-Cloud-Scheduler 实例必须使用相同的 ZooKeeper 集群。
例如，如果 ZooKeeper 的 Quorum 为 zk://1.2.3.4:2181,2.3.4.5:2181,3.4.5.6:2181/elasticjob-cloud，则 `elasticjob-cloud-scheduler.properties` 中 ZooKeeper 相关配置为：

```properties
# ElasticJob-Cloud's ZooKeeper address
zk_servers=1.2.3.4:2181,2.3.4.5:2181,3.4.5.6:2181

# ElasticJob-Cloud's ZooKeeper namespace
zk_namespace=elasticjob-cloud
```
