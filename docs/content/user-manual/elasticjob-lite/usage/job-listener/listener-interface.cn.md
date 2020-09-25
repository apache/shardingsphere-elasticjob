+++
title = "监听器开发"
weight = 1
chapter = true
+++

## 常规监听器

若作业处理作业服务器的文件，处理完成后删除文件，可考虑使用每个节点均执行清理任务。
此类型任务实现简单，且无需考虑全局分布式任务是否完成，应尽量使用此类型监听器。

```java

public class MyJobListener implements ElasticJobListener {
    
    @Override
    public void beforeJobExecuted(ShardingContexts shardingContexts) {
        // do something ...
    }
    
    @Override
    public void afterJobExecuted(ShardingContexts shardingContexts) {
        // do something ...
    }
    
    @Override
    public String getType() {
        return "simpleJobListener";
    }
}
```

## 分布式监听器

若作业处理数据库数据，处理完成后只需一个节点完成数据清理任务即可。
此类型任务处理复杂，需同步分布式环境下作业的状态同步，提供了超时设置来避免作业不同步导致的死锁，应谨慎使用。

```java

public class MyDistributeOnceJobListener extends AbstractDistributeOnceElasticJobListener {
    
    public TestDistributeOnceElasticJobListener(long startTimeoutMills, long completeTimeoutMills) {
        super(startTimeoutMills, completeTimeoutMills);
    }
    
    @Override
    public void doBeforeJobExecutedAtLastStarted(ShardingContexts shardingContexts) {
        // do something ...
    }
    
    @Override
    public void doAfterJobExecutedAtLastCompleted(ShardingContexts shardingContexts) {
        // do something ...
    }
    
    @Override
    public String getType() {
        return "distributeOnceJobListener";
    }
}
```

## 添加SPI实现

将JobListener实现添加至infra-common下resources/META-INF/services/org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener
