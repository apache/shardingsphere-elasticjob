+++
pre = "<b>1. </b>"
title = "概览"
weight = 1
chapter = true
+++

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere-elasticjob.svg?style=social&label=Release)](https://github.com/apache/shardingsphere-elasticjob/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/apache/shardingsphere-elasticjob.svg?style=social&label=Star)](https://github.com/apache/shardingsphere-elasticjob/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/apache/shardingsphere-elasticjob.svg?style=social&label=Fork)](https://github.com/apache/shardingsphere-elasticjob/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/apache/shardingsphere-elasticjob.svg?style=social&label=Watch)](https://github.com/apache/shardingsphere-elasticjob/watchers)
[![Stargazers over time](https://starchart.cc/apache/shardingsphere-elasticjob.svg)](https://starchart.cc/apache/shardingsphere-elasticjob)

ElasticJob 通过弹性调度、资源管控、以及作业治理的功能，打造一个适用于互联网场景的分布式调度解决方案，并通过开放的架构设计，提供多元化的作业生态。
它的各个产品使用统一的作业 API，开发者仅需一次开发，即可随意部署。

ElasticJob 已于 2020 年 5 月 28 日成为 [Apache ShardingSphere](https://shardingsphere.apache.org/) 的子项目。
欢迎通过[邮件列表](mailto:dev@shardingsphere.apache.org)参与讨论。

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere-elasticjob.svg)](https://github.com/apache/shardingsphere-elasticjob/releases)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/org.apache.shardingsphere.elasticjob/elasticjob/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.shardingsphere.elasticjob/elasticjob)
[![Build Status](https://secure.travis-ci.org/apache/shardingsphere-elasticjob.png?branch=master)](https://travis-ci.org/apache/shardingsphere-elasticjob)
[![Coverage Status](https://coveralls.io/repos/github/apache/shardingsphere-elasticjob/badge.svg?branch=master)](https://coveralls.io/github/apache/shardingsphere-elasticjob?branch=master)

## 简介

使用 ElasticJob 能够让开发工程师不再担心任务的线性吞吐量提升等非功能需求，使他们能够更加专注于面向业务编码设计；
同时，它也能够解放运维工程师，使他们不必再担心任务的可用性和相关管理需求，只通过轻松的增加服务节点即可达到自动化运维的目的。

ElasticJob 定位为轻量级无中心化解决方案，使用 jar 的形式提供分布式任务的协调服务。

![ElasticJob Architecture](https://shardingsphere.apache.org/elasticjob/current/img/architecture/elasticjob_lite.png)

## 功能列表

- 弹性调度
  - 支持任务在分布式场景下的分片和高可用
  - 能够水平扩展任务的吞吐量和执行效率
  - 任务处理能力随资源配备弹性伸缩

- 资源分配
  - 在适合的时间将适合的资源分配给任务并使其生效
  - 相同任务聚合至相同的执行器统一处理
  - 动态调配追加资源至新分配的任务

- 作业治理
  - 失效转移
  - 错过作业重新执行
  - 自诊断修复

- 作业依赖(TODO)
  - 基于有向无环图（DAG）的作业间依赖
  - 基于有向无环图（DAG）的作业分片间依赖

- 作业开放生态
  - 可扩展的作业类型统一接口
  - 丰富的作业类型库，如数据流、脚本、HTTP、文件、大数据等
  - 易于对接业务作业，能够与 Spring 依赖注入无缝整合

- 可视化管控端
  - 作业管控端
  - 作业执行历史数据追踪
  - 注册中心管理

## 环境要求

### Java

请使用 Java 8 及其以上版本。

### Maven

请使用 Maven 3.5.0 及其以上版本。

### ZooKeeper

请使用 ZooKeeper 3.6.0 及其以上版本。[详情参见](https://zookeeper.apache.org/)
