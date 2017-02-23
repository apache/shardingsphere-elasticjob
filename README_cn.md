##Elastic-Job - distributed scheduled job solution

# [English](README.md)

# [原1.x版本文档](README_1.x.md)

[![Hex.pm](http://dangdangdotcom.github.io/elastic-job/img/license.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job)
[![Build Status](https://secure.travis-ci.org/dangdangdotcom/elastic-job.png?branch=master)](https://travis-ci.org/dangdangdotcom/elastic-job)
[![Coverage Status](https://coveralls.io/repos/dangdangdotcom/elastic-job/badge.svg?branch=master&service=github)](https://coveralls.io/github/dangdangdotcom/elastic-job?branch=master)

# 概览

Elastic-Job是一个分布式调度解决方案，由两个相互独立的子项目Elastic-Job-Lite和Elastic-Job-Cloud组成。

Elastic-Job-Lite定位为轻量级无中心化解决方案，使用jar包的形式提供分布式任务的协调服务。
Elastic-Job-Cloud使用Mesos + Docker(TBD)的解决方案，额外提供资源治理、应用分发以及进程隔离等服务。

Elastic-Job-Lite和Elastic-Job-Cloud提供同一套API开发作业，开发者仅需一次开发，即可根据需要以Lite或Cloud的方式部署。

# 为何使用Elastic-Job？

## 通用部分

### 1. 分片概念

任务的分布式执行，需要将一个任务拆分为n个独立的任务项，然后由分布式的服务器分别执行某一个或几个分片项。

例如：有一个遍历数据库某张表的作业，现有`2`台服务器。为了快速的执行作业，那么每台服务器应执行作业的`50%`。
为满足此需求，可将作业分成`2`片，每台服务器执行`1`片。作业遍历数据的逻辑应为：服务器`A`遍历`ID`以奇数结尾的数据；服务器`B`遍历`ID`以偶数结尾的数据。
如果分成`10`片，则作业遍历数据的逻辑应为：每片分到的分片项应为`ID%10`，而服务器A被分配到分片项`0,1,2,3,4`；服务器B被分配到分片项`5,6,7,8,9`，直接的结果就是服务器`A`遍历`ID`以`0-4`结尾的数据；服务器`B`遍历`ID`以`5-9`结尾的数据。

### 2. 分片项与业务处理解耦

`Elastic-Job`并不直接提供数据处理的功能，框架只会将分片项分配至各个运行中的作业服务器，开发者需要自行处理分片项与真实数据的对应关系。

### 3. 个性化参数的适用场景

个性化参数即`shardingItemParameter`，可以和分片项匹配对应关系，用于将分片项的数字转换为更加可读的业务代码。

例如：按照地区水平拆分数据库，数据库`A`是北京的数据；数据库`B`是上海的数据；数据库`C`是广州的数据。
如果仅按照分片项配置，开发者需要了解`0`表示北京；`1`表示上海；`2`表示广州。
合理使用个性化参数可以让代码更可读，如果配置为`0=北京`,`1=上海`,`2=广州`，那么代码中直接使用北京，上海，广州的枚举值即可完成分片项和业务逻辑的对应关系。

## Elastic-Job-Lite

### 1. 分布式调度

`Elastic-Job-Lite`并无作业调度中心节点，而是基于部署作业框架的程序在到达相应时间点时各自触发调度。

注册中心仅用于作业注册和监控信息存储。而主作业节点仅用于处理分片和清理等功能。

### 2. 作业高可用

`Elastic-Job-Lite`提供最安全的方式执行作业。将分片总数设置为`1`，并使用多于`1`台的服务器执行作业，作业将会以`1`主`n`从的方式执行。

一旦执行作业的服务器崩溃，等待执行的服务器将会在下次作业启动时替补执行。开启失效转移功能效果更好，可以保证在本次作业执行时崩溃，备机立即启动替补执行。

### 3. 最大限度利用资源

`Elastic-Job-Lite`也提供最灵活的方式，最大限度的提高执行作业的吞吐量。将分片项设置为大于服务器的数量，最好是大于服务器倍数的数量，作业将会合理的利用分布式资源，动态的分配分片项。

例如：`3`台服务器，分成`10`片，则分片项分配结果为服务器`A=0,1,2`;服务器`B=3,4,5`;服务器`C=6,7,8,9`。
如果服务器`C`崩溃，则分片项分配结果为服务器`A=0,1,2,3,4`;服务器`B=5,6,7,8,9`。在不丢失分片项的情况下，最大限度的利用现有资源提高吞吐量。

## Elastic-Job-Cloud

### 1. 分布式调度

`Elastic-Job-Cloud`采用`Mesos Framework`分片和协调作业调度。采用中心化调度实现难度小于`Elastic-Job-Lite`的无中心化调度，无需再考虑多线程并发的情况。

### 2. 作业高可用

`Elastic-Job-Cloud`由`Mesos Framework`负责作业高可用和分片。作业丢失会由`Mesos Framework`自动在另外的`Agent`上重新启动作业分片实例。

### 3. 弹性资源利用

`Elastic-Job-Cloud`分为`2`种作业运行模式：瞬时作业 和 常驻作业。

瞬时作业会在每一次作业执行完毕后立刻释放资源，保证利用现有资源错峰执行。资源分配和容器启动均占用一定时长，且作业执行时资源不一定充足，因此作业执行会有延迟。瞬时作业适用于间隔时间长，资源消耗多且对执行时间无严格要求的作业。

常驻作业无论在运行时还是等待运行时，均一直占用分配的资源，可节省过多容器启动和资源分配的开销，适用于间隔时间短，资源需求量稳定的作业。

# 功能列表

## 1. Elastic-Job-Lite

* 分布式调度协调
* 弹性扩容缩容
* 失效转移
* 错过执行作业重触发
* 作业分片一致性，保证同一分片在分布式环境中仅一个执行实例
* 支持并行调度
* 支持作业声明周期操作
* 丰富的作业类型
* Spring整合以及命名空间提供
* 运维平台

## 2. Elastic-Job-Cloud
* 包含Elastic-Job-Lite的全部功能
* 应用自动分发
* 基于Fenzo的弹性资源分配
* 基于Docker的进程隔离(TBD)

***

# [Roadmap](ROADMAP.md)

# [Release Notes](http://dangdangdotcom.github.io/elastic-job/post/release_notes/)

# Architecture

## Elastic-Job-Lite

![Elastic-Job-Lite Architecture](elastic-job-doc/content/img/architecture/elastic_job_lite.png)

***

## Elastic-Job-Cloud

![Elastic-Job-Lite Architecture](elastic-job-doc/content/img/architecture/elastic_job_cloud.png)

# Quick Start

## Elastic-Job-Lite

### 引入maven依赖

```xml
<!-- 引入elastic-job-lite核心模块 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>

<!-- 使用springframework自定义命名空间时引入 -->
<dependency>
    <groupId>com.dangdang</groupId>
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
    <reg:zookeeper id="regCenter" server-lists=" yourhost:2181" namespace="dd-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
    
    <!-- 配置作业-->
    <job:simple id="oneOffElasticJob" class="xxx.MyElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
</beans>
```
***

## Elastic-Job-Cloud

### 引入maven依赖

```xml
<!-- 引入elastic-job-cloud执行器模块 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-cloud-executor</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```
### 作业开发

同`Elastic-Job-Lite`

### 作业APP配置

```shell
curl -l -H "Content-type: application/json" -X POST -d 
'{"appName":"yourAppName","appURL":"http://app_host:8080/foo-job.tar.gz","cpuCount":0.1,"memoryMB":64.0,"bootstrapScript":"bin/start.sh","appCacheEnable":true}' 
http://elastic_job_cloud_host:8899/app
```

### 作业配置

```shell
curl -l -H "Content-type: application/json" -X POST -d 
'{"jobName":"fooJob","appName":"yourAppName","jobClass":"yourJobClass","jobType":"SIMPLE","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"failover":true,"misfire":true}' 
http://elastic_job_cloud_host:8899/job/register
```

***

# 相关文档

## 1. Elastic-Job-Lite
   * [使用指南](http://dangdangdotcom.github.io/elastic-job/post/user_guide/lite/lite_index)      
   * [实现原理](http://dangdangdotcom.github.io/elastic-job/post/principles/lite/)

## 2. Elastic-Job-Cloud
   * [使用指南](http://dangdangdotcom.github.io/elastic-job/post/user_guide/cloud/cloud_index)
   * 实现原理(TBD)

## 3. [FAQ](http://dangdangdotcom.github.io/elastic-job/post/faq/)

## 4. [采用公司](http://dangdangdotcom.github.io/elastic-job/post/companies_using/)

## 5. [其他第三方文档](http://dangdangdotcom.github.io/elastic-job/post/third_parties_docs/)

**讨论QQ群：**430066234（不限于Elastic-Job，包括分布式，定时任务相关以及其他互联网技术交流。由于QQ群已接近饱和，我们希望您在申请加群之前仔细阅读文档，并在加群申请中正确回答问题，以及在申请时写上您的姓名和公司名称。并且在入群后及时修改群名片。否则我们将有权拒绝您的入群申请。谢谢合作。）
