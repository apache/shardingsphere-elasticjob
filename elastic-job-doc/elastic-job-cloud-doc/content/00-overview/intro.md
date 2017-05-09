+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "简介"
weight = 1
prev = "/00-overview"
next = "/00-overview/contribution/"
+++

# 为何使用Elastic-Job-Cloud？

Elastic-Job-Cloud以私有云平台的方式提供集资源、调度以及分片为一体的全量级解决方案，依赖Mesos和Zookeeper。

# 基本概念

### 1. 分片

任务的分布式执行，需要将一个任务拆分为多个独立的任务项，然后由分布式的服务器分别执行某一个或几个分片项。

例如：有一个遍历数据库某张表的作业，现有2台服务器。为了快速的执行作业，那么每台服务器应执行作业的50%。
为满足此需求，可将作业分成2片，每台服务器执行1片。作业遍历数据的逻辑应为：服务器A遍历ID以奇数结尾的数据；服务器B遍历ID以偶数结尾的数据。
如果分成10片，则作业遍历数据的逻辑应为：每片分到的分片项应为ID%10，而服务器A被分配到分片项0,1,2,3,4；服务器B被分配到分片项5,6,7,8,9，直接的结果就是服务器A遍历ID以0-4结尾的数据；服务器B遍历ID以5-9结尾的数据。

### 2. 分片项与业务处理解耦

Elastic-Job并不直接提供数据处理的功能，框架只会将分片项分配至各个运行中的作业服务器，开发者需要自行处理分片项与真实数据的对应关系。

### 3. 个性化参数的适用场景

个性化参数即shardingItemParameter，可以和分片项匹配对应关系，用于将分片项的数字转换为更加可读的业务代码。

例如：按照地区水平拆分数据库，数据库A是北京的数据；数据库B是上海的数据；数据库C是广州的数据。
如果仅按照分片项配置，开发者需要了解0表示北京；1表示上海；2表示广州。
合理使用个性化参数可以让代码更可读，如果配置为0=北京,1=上海,2=广州，那么代码中直接使用北京，上海，广州的枚举值即可完成分片项和业务逻辑的对应关系。

# 核心理念

### 1. 分布式调度

Elastic-Job-Cloud采用Mesos Framework分片和协调作业调度。采用中心化调度实现难度小于Elastic-Job-Lite的无中心化调度，无需再考虑多线程并发的情况。

### 2. 作业高可用

Elastic-Job-Cloud由Mesos Framework负责作业高可用和分片。作业丢失会由Mesos Framework自动在另外的Agent上重新启动作业分片实例。

### 3. 弹性资源利用

Elastic-Job-Cloud分为2种作业运行模式：瞬时作业 和 常驻作业。

瞬时作业会在每一次作业执行完毕后立刻释放资源，保证利用现有资源错峰执行。资源分配和容器启动均占用一定时长，且作业执行时资源不一定充足，因此作业执行会有延迟。瞬时作业适用于间隔时间长，资源消耗多且对执行时间无严格要求的作业。

常驻作业无论在运行时还是等待运行时，均一直占用分配的资源，可节省过多容器启动和资源分配的开销，适用于间隔时间短，资源需求量稳定的作业。

# 整体架构图

![Elastic-Job-Cloud Architecture](/img/architecture/elastic_job_cloud.png)

# 快速入门

## 引入maven依赖

```xml
<!-- 引入elastic-job-cloud执行器模块 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-cloud-executor</artifactId>
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

## 打包作业
tar -cvf yourJobs.tar.gz yourJobs

## 发布APP

```shell
curl -l -H "Content-type: application/json" -X POST -d '{"appName":"foo_app","appURL":"http://app_host:8080/yourJobs.gz","cpuCount":0.1,"memoryMB":64.0,"bootstrapScript":"bin/start.sh","appCacheEnable":true,"eventTraceSamplingCount":0}' http://elastic_job_cloud_host:8899/api/app
```

## 发布作业

```shell
curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"foo_job","jobClass":"yourJobClass","jobType":"SIMPLE","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appName":"foo_app","failover":true,"misfire":true,"bootstrapScript":"bin/start.sh"}' http://elastic_job_cloud_host:8899/api/job/register
```
