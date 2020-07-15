# [ElasticJob - 分布式作业调度解决方案](http://shardingsphere.apache.org/elasticjob/)

**官方网站: https://shardingsphere.apache.org/elasticjob/**

[![Stargazers over time](https://starchart.cc/apache/shardingsphere-elasticjob-lite.svg)](https://starchart.cc/apache/shardingsphere-elasticjob-lite)

ElasticJob 是一个分布式调度解决方案，由 2 个相互独立的子项目 ElasticJob Lite 和 ElasticJob Cloud 组成。

ElasticJob Lite 定位为轻量级无中心化解决方案，使用 jar 的形式提供分布式任务的协调服务；
ElasticJob Cloud 使用 Mesos + Docker（TODO）的解决方案，额外提供资源治理、应用分发以及进程隔离等服务。

ElasticJob 的各个产品使用统一的作业 API，开发者仅需要一次开发，即可随意部署。

ElasticJob 已于 2020 年 5 月 28 日成为 [Apache ShardingSphere](https://shardingsphere.apache.org/) 的子项目。
欢迎通过[邮件列表](mailto:dev@shardingsphere.apache.org)参与讨论。

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere-elasticjob-lite.svg)](https://github.com/apache/shardingsphere-elasticjob-lite/releases)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job)
[![Build Status](https://secure.travis-ci.org/apache/shardingsphere-elasticjob-lite.png?branch=master)](https://travis-ci.org/apache/shardingsphere-elasticjob-lite)
[![Coverage Status](https://coveralls.io/repos/elasticjob/elastic-job/badge.svg?branch=master&service=github)](https://coveralls.io/github/elasticjob/elastic-job?branch=master)

## 架构图

### ElasticJob Lite

![ElasticJob Lite Architecture](https://shardingsphere.apache.org/elasticjob/current/img/architecture/elasticjob_lite.png)

### ElasticJob Cloud

![ElasticJob Lite Architecture](https://shardingsphere.apache.org/elasticjob/current/img/architecture/elasticjob_cloud.png)

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
