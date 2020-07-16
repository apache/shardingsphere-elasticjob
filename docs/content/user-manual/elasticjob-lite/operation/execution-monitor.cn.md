+++
title = "作业运行状态监控"
weight = 4
chapter = true
+++

通过监听 ElasticJob-Lite 的 ZooKeeper 注册中心的几个关键节点即可完成作业运行状态监控功能。

## 监听作业服务器存活

监听 job_name\instances\job_instance_id 节点是否存在。该节点为临时节点，如果作业服务器下线，该节点将删除。
