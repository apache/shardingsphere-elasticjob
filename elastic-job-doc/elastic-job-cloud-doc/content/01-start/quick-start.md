+++
toc = true
date = "2017-04-06T22:38:50+08:00"
title = "快速入门"
weight = 1
prev = "/01-start"
next = "/01-start/faq/"
+++

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

## Java启动方式

需定义`Main`方法并调用`JobBootstrap.execute()`，例子如下：

```java
public class JobDemo {
    
    public static void main(final String[] args) {
        JobBootstrap.execute();
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

>详细的开发方式请参考[开发指南](/02-guide/dev-guide)