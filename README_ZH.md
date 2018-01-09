# Elastic-Job - 分布式作业调度解决方案

[![Build Status](https://secure.travis-ci.org/elasticjob/elastic-job-lite.png?branch=master)](https://travis-ci.org/elasticjob/elastic-job-lite)
[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job-lite/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job-lite)
[![Gitter](https://badges.gitter.im/Elastic-JOB/elastic-job-lite.svg)](https://gitter.im/Elastic-JOB/elasticjob?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Coverage Status](https://coveralls.io/repos/elasticjob/elastic-job/badge.svg?branch=master&service=github)](https://coveralls.io/github/elasticjob/elastic-job?branch=master)
[![GitHub release](https://img.shields.io/github/release/elasticjob/elastic-job.svg)](https://github.com/elasticjob/elastic-job/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# 概述

Elastic-Job是一个分布式调度解决方案，由两个相互独立的子项目Elastic-Job-Lite和Elastic-Job-Cloud组成。

Elastic-Job-Lite定位为轻量级无中心化解决方案，使用jar包的形式提供分布式任务的协调服务。

# 功能列表

* 分布式调度协调
* 弹性扩容缩容
* 失效转移
* 错过执行作业重触发
* 作业分片一致性，保证同一分片在分布式环境中仅一个执行实例
* 自诊断并修复分布式不稳定造成的问题
* 支持并行调度
* 支持作业生命周期操作
* 丰富的作业类型
* Spring整合以及命名空间提供
* 运维平台

# 架构图

## Elastic-Job-Lite

![Elastic-Job-Lite Architecture](http://ovfotjrsi.bkt.clouddn.com/docs/img/architecture/elastic_job_lite.png)


# [Release Notes](https://github.com/elasticjob/elastic-job/releases)

# [Roadmap](ROADMAP.md)

# 快速入门

## 引入maven依赖

```xml
<!-- 引入elastic-job-lite核心模块 -->
<dependency>
    <groupId>io.elasticjob</groupId>
    <artifactId>elastic-job-lite-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>

<!-- 使用springframework自定义命名空间时引入 -->
<dependency>
    <groupId>io.elasticjob</groupId>
    <artifactId>elastic-job-lite-spring</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

## 作业开发

```java
public class MyElasticJob implements SimpleJob {
    
    @Override
    public void execute(ShardingContext context) {
        switch (context.getShardingItem()) {
            case 0: 
                // do something by sharding item 0
                break;
            case 1: 
                // do something by sharding item 1
                break;
            case 2: 
                // do something by sharding item 2
                break;
            // case n: ...
        }
    }
}
```

## 作业配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:reg="http://www.dangdang.com/schema/ddframe/reg"
    xmlns:job="http://www.dangdang.com/schema/ddframe/job"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.dangdang.com/schema/ddframe/reg
                        http://www.dangdang.com/schema/ddframe/reg/reg.xsd
                        http://www.dangdang.com/schema/ddframe/job
                        http://www.dangdang.com/schema/ddframe/job/job.xsd
                        ">
    <!--配置作业注册中心 -->
    <reg:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="dd-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
    
    <!-- 配置作业-->
    <job:simple id="oneOffElasticJob" class="xxx.MyElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
</beans>
```
