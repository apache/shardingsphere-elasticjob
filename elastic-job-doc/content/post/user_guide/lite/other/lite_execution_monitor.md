+++
date = "2016-01-27T16:14:21+08:00"
title = "Elastic-Job-Lite作业运行状态监控"
weight=22
+++

# Elastic-Job-Lite作业运行状态监控

通过监听`Elastic-Job-Lite`的`zookeeper`注册中心的几个关键节点即可完成作业运行状态监控功能。

## 监听作业服务器存活

监听`job_name\servers\ip_address\status`节点是否存在。该节点为临时节点，如果作业服务器下线，该节点将删除。
