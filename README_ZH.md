# [ElasticJob - 分布式作业调度解决方案](http://shardingsphere.apache.org/elasticjob/)

**官方网站: http://shardingsphere.apache.org/elasticjob/**

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

![Elastic-Job-Lite Architecture](https://shardingsphere.apache.org/elasticjob/lite/img/architecture/elastic_job_lite.png)

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

## [Release Notes](https://github.com/elasticjob/elastic-job/releases)

## [Roadmap](ROADMAP.md)

## 快速入门

### 引入maven依赖

```xml
<!-- 引入elastic-job-lite核心模块 -->
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elastic-job-lite-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>

<!-- 使用springframework自定义命名空间时引入 -->
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elastic-job-lite-spring</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### 作业开发

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

### 作业配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:elasticjob="http://elasticjob.shardingsphere.apache.org/schema/elasticjob"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://elasticjob.shardingsphere.apache.org/schema/elasticjob
                           http://elasticjob.shardingsphere.apache.org/schema/elasticjob/elasticjob.xsd
                           ">
    <!--配置作业注册中心 -->
    <elasticjob:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="elastic-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
   
    <!--配置任务快照服务 -->
    <elasticjob:snapshot id="jobSnapshot" registry-center-ref="regCenter" dump-port="9999"/>

    <!--配置作业类 -->
    <bean id="simpleJob" class="xxx.MyElasticJob" />
    
    <!--配置作业 -->
    <elasticjob:simple id="oneOffElasticJob" job-ref="simpleJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
</beans>
```
