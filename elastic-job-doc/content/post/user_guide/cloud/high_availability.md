+++
date = "2016-02-15T16:14:21+08:00"
title = "Elastic-Job-Cloud-Scheduler高可用模式"
weight=55
+++

# Elastic-Job-Cloud-Scheduler高可用模式

## 介绍

`Elastic-Job-Cloud-Scheduler`只能运行在高可用模式下。高可用模式允许调度器在单个实例变得不可用时继续运行。
这是通过运行几个指向同一个ZooKeeper集群的`Elastic-Job-Cloud-Scheduler`实例来实现的。 `ZooKeeper`用于在当前主`Elastic-Job-Cloud-Scheduler`实例失败的情况下执行领导者选举。

## 配置

`Elastic-Job-Cloud-Scheduler`在高可用模式下运行不需要额外的配置，且高可用模式不可以禁用。
每个`Elastic-Job-Cloud-Scheduler`实例必须使用相同的ZooKeeper集群。
例如，如果`Zookeeper`的`Quorum`为```zk://1.2.3.4:2181,2.3.4.5:2181,3.4.5.6:2181/elastic-job-cloud```，则`elastic-job-cloud-scheduler.properties`中`Zookeeper`相关配置为：

```properties
# Elastic-Job-Cloud's zookeeper address
zk_servers=1.2.3.4:2181,2.3.4.5:2181,3.4.5.6:2181

# Elastic-Job-Cloud's zookeeper namespace
zk_namespace=elastic-job-cloud
```