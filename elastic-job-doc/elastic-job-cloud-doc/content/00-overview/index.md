+++
icon = "<b>0. </b>"
date = "2017-04-13T16:06:17+08:00"
title = "概览"
weight = 0
prev = "/03-design/roadmap/"
next = "/00-overview/intro/"
chapter = true
+++

# 概述

[![GitHub release](https://img.shields.io/github/release/dangdangdotcom/elastic-job.svg?style=social&label=Release)](https://github.com/dangdangdotcom/elastic-job/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/dangdangdotcom/elastic-job.svg?style=social&label=Star)](https://github.com/dangdangdotcom/elastic-job/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/dangdangdotcom/elastic-job.svg?style=social&label=Fork)](https://github.com/dangdangdotcom/elastic-job/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/dangdangdotcom/elastic-job.svg?style=social&label=Watch)](https://github.com/dangdangdotcom/elastic-job/watchers)

Elastic-Job是一个分布式调度解决方案，由两个相互独立的子项目Elastic-Job-Lite和Elastic-Job-Cloud组成。

Elastic-Job-Cloud使用Mesos + Docker的解决方案，额外提供资源治理、应用分发以及进程隔离等服务。

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job)
[![Build Status](https://secure.travis-ci.org/dangdangdotcom/elastic-job.png?branch=master)](https://travis-ci.org/dangdangdotcom/elastic-job)
[![Coverage Status](https://coveralls.io/repos/dangdangdotcom/elastic-job/badge.svg?branch=master&service=github)](https://coveralls.io/github/dangdangdotcom/elastic-job?branch=master)
[![Hex.pm](http://dangdangdotcom.github.io/elastic-job/img/license.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# 功能列表

* 应用自动分发
* 基于Fenzo的弹性资源分配
* 分布式调度协调
* 弹性扩容缩容
* 失效转移
* 错过执行作业重触发
* 作业分片一致性，保证同一分片在分布式环境中仅一个执行实例
* 支持并行调度
* 支持作业生命周期操作
* 丰富的作业类型
* Spring整合
* 运维平台
* 基于Docker的进程隔离(TBD)

# 第三方文档

* 2017-04 [中国技术开放日分享：如何从0到1搭建弹性作业云Elastic-Job-Cloud](http://www.infoq.com/cn/presentations/how-to-build-elastic-job-cloud)

* 2016-11 [2016全球容器技术大会分享：基于Mesos的当当作业云Elastic-Job-Cloud](http://www.infoq.com/cn/presentations/dangdang-operating-cloud-elastic-job-cloud-based-on-the-esos)

* 2016-09 [InfoQ新闻：基于Mesos的当当作业云Elastic Job Cloud](http://www.infoq.com/cn/news/2016/09/Mesos-Elastic-Job-Cloud)

# 交流与参与

- **讨论QQ群：** 430066234（不限于Elastic-Job，包括分布式，定时任务相关以及其他互联网技术交流。由于QQ群已接近饱和，我们希望您在申请加群之前仔细阅读文档，并在加群申请中正确回答问题，以及在申请时写上您的姓名和公司名称。并且在入群后及时修改群名片。否则我们将有权拒绝您的入群申请。谢谢合作。）
- 报告确定的bug，提交增强功能建议和提交补丁等，请阅读[如何进行贡献](/00-overview/contribution)。

# 采用公司（统计中）
