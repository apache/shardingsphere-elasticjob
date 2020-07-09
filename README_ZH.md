# [ElasticJob - 分布式作业调度解决方案](http://shardingsphere.apache.org/elasticjob/)

**官方网站: http://shardingsphere.apache.org/elasticjob/**

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

![ElasticJob Lite Architecture](https://shardingsphere.apache.org/elasticjob/lite/img/architecture/elastic_job_lite.png)

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
  - 自巡检与自动修复

- 作业依赖(TODO)
  - 基于有向无环图（DAG）的作业间依赖
  - 基于有向无环图（DAG）的作业分片间依赖

- 作业开放生态
  - 可扩展的作业类型统一接口
  - 丰富的作业类型库，如数据流、脚本、HTTP、文件、大数据等
  - 面向业务的作业接口，能够与 Spring 依赖注入 无缝对接

- 可视化管控端
  - 作业管控端
  - 作业执行历史数据追踪
  - 注册中心管理


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
