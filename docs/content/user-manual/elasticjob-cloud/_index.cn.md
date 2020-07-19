+++
pre = "<b>4.2. </b>"
title = "ElasticJob-Cloud"
weight = 2
chapter = true
+++

## 简介

ElasticJob-Cloud 采用自研 Mesos Framework 的解决方案，额外提供资源治理、应用分发以及进程隔离等功能。

![ElasticJob-Cloud Architecture](https://shardingsphere.apache.org/elasticjob/current/img/architecture/elasticjob_cloud.png)

## 对比

|           | *ElasticJob-Lite* | *ElasticJob-Cloud* |
| --------- | ----------------- | ------------------ |
| 无中心化   | 是                | `否`                |
| 资源分配   | 不支持             | `支持`              |
| 作业模式   | 常驻               | `常驻 + 瞬时`       |
| 部署依赖   | ZooKeeper         | `ZooKeeper + Mesos` |

ElasticJob-Cloud 的优势在于对资源细粒度治理，适用于需要削峰填谷的大数据系统。
