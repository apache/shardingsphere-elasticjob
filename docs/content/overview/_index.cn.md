+++
pre = "<b>1. </b>"
title = "概览"
weight = 1
chapter = true
+++

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere-elasticjob-lite.svg?style=social&label=Release)](https://github.com/apache/shardingsphere-elasticjob-lite/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/apache/shardingsphere-elasticjob-lite.svg?style=social&label=Star)](https://github.com/apache/shardingsphere-elasticjob-lite/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/apache/shardingsphere-elasticjob-lite.svg?style=social&label=Fork)](https://github.com/apache/shardingsphere-elasticjob-lite/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/apache/shardingsphere-elasticjob-lite.svg?style=social&label=Watch)](https://github.com/apache/shardingsphere-elasticjob-lite/watchers)
[![Stargazers over time](https://starchart.cc/apache/shardingsphere-elasticjob-lite.svg)](https://starchart.cc/apache/shardingsphere-elasticjob-lite)

ElasticJob 是一个分布式调度解决方案，由 2 个相互独立的子项目 ElasticJob Lite 和 ElasticJob Cloud 组成。

ElasticJob Lite 定位为轻量级无中心化解决方案，使用 jar 的形式提供分布式任务的协调服务。

ElasticJob 已于 2020 年 5 月 28 日成为 [Apache ShardingSphere](https://shardingsphere.apache.org/)的子项目。
欢迎通过[邮件列表](mailto:dev@shardingsphere.apache.org)参与讨论。

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere-elasticjob-lite.svg)](https://github.com/apache/shardingsphere-elasticjob-lite/releases)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job)
[![Build Status](https://secure.travis-ci.org/apache/shardingsphere-elasticjob-lite.png?branch=master)](https://travis-ci.org/apache/shardingsphere-elasticjob-lite)
[![Coverage Status](https://coveralls.io/repos/elasticjob/elastic-job/badge.svg?branch=master&service=github)](https://coveralls.io/github/elasticjob/elastic-job?branch=master)

## 架构图

### ElasticJob Lite

![ElasticJob Lite Architecture](https://shardingsphere.apache.org/elasticjob/lite/img/architecture/elastic_job_lite.png)

## 功能列表

* 分布式调度协调
* 弹性扩容缩容
* 失效转移
* 错过执行作业重触发
* 作业分片一致性，保证同一分片在分布式环境中仅一个执行实例
* 自诊断并修复分布式不稳定造成的问题
* 支持并行调度
* 支持作业生命周期操作
* 丰富的作业类型
* Spring 整合以及命名空间提供
* 运维平台
