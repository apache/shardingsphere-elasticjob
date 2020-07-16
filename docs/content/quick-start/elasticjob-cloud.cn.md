+++
pre = "<b>2.2. </b>"
title = "ElasticJob-Cloud"
weight = 2
chapter = true
+++

## 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-cloud-executor</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

## 作业开发

```java
public class MyJob implements SimpleJob {
    
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

## 作业启动

需定义 `main` 方法并调用 `JobBootstrap.execute()`，例子如下：

```java
public class MyJobDemo {
    
    public static void main(final String[] args) {
        JobBootstrap.execute(new MyJob());
    }
}
```

## 作业打包

```bash
tar -cvf my-job.tar.gz my-job
```

## 作业发布

```bash
curl -l -H "Content-type: application/json" -X POST -d '{"appName":"my_app","appURL":"http://app_host:8080/my-job.tar.gz","cpuCount":0.1,"memoryMB":64.0,"bootstrapScript":"bin/start.sh","appCacheEnable":true,"eventTraceSamplingCount":0}' http://elasticjob_cloud_host:8899/api/app
```

## 作业调度

```bash
curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"my_job","appName":"my_app","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":3,"cpuCount":0.1,"memoryMB":64.0}' http://elasticjob_cloud_host:8899/api/job/register
```
