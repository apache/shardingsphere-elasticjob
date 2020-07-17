+++
pre = "<b>4.1. </b>"
title = "ElasticJob-Lite"
weight = 1
chapter = true
+++

## 简介

ElasticJob-Lite 定位为轻量级无中心化解决方案，使用 jar 的形式提供分布式任务的协调服务。

![ElasticJob-Lite Architecture](https://shardingsphere.apache.org/elasticjob/current/img/architecture/elasticjob_lite.png)

## 对比

|           | *ElasticJob-Lite* | *ElasticJob-Cloud* |
| --------- | ----------------- | ------------------ |
| 无中心化   | `是`              | 否                  |
| 资源分配   | `不支持`           | 支持                |
| 作业模式   | `常驻`             | 常驻 + 瞬时         |
| 部署依赖   | `ZooKeeper`       | ZooKeeper + Mesos   |

ElasticJob-Lite 的优势在于无中心化设计且外部依赖少，适用于资源分配稳定的业务系统。
