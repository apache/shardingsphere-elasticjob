+++
date = "2016-01-27T16:14:21+08:00"
title = "1.x 作业运行状态监控"
weight=1012
+++

# 作业运行状态监控

通过监听`elastic-job`的`zookeeper`注册中心的几个关键节点即可完成作业运行状态监控功能。

## 监听作业服务器存活

监听`job_name\servers\ip_address\status`节点是否存在。该节点为临时节点，如果作业服务器下线，该节点将删除。

## 监听近期数据处理成功

数据流类型作业，可通过监听近期数据处理成功数判断作业流量是否正常。

监听`job_name\servers\ip_address\processSuccessCount`节点的值。如果小于作业正常处理的阀值，可选择报警。

## 监听近期数据处理失败

数据流类型作业，可通过监听近期数据处理失败数判断作业处理结果。

监听`job_name\servers\ip_address\processFailureCount`节点的值。如果大于`0`，可选择报警。