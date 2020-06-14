+++
toc = true
date = "2016-01-27T16:14:21+08:00"
title = "作业运行状态监控"
weight = 22
prev = "/02-guide/event-trace/"
next = "/02-guide/dump/"
+++

通过监听Elastic-Job-Lite的zookeeper注册中心的几个关键节点即可完成作业运行状态监控功能。

## 监听作业服务器存活

监听job_name\instances\job_instance_id节点是否存在。该节点为临时节点，如果作业服务器下线，该节点将删除。
