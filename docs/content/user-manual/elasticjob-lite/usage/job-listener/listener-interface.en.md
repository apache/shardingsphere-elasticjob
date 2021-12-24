+++
title = "Listener Development"
weight = 1
chapter = true
+++

## Common Listener

If the job processes the files of the job server and deletes the files after the processing is completed, consider using each node to perform the cleaning task.
This type of task is simple to implement, and there is no need to consider whether the global distributed task is completed. You should try to use this type of listener.

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

## Distributed Listener

If the job processes database data, only one node needs to complete the data cleaning task after the processing is completed.
This type of task is complicated to process and needs to synchronize the status of the job in a distributed environment. Timeout settings are provided to avoid deadlocks caused by job out of sync. It should be used with caution.

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

## Add SPI implementation

Put JobListener implementation to module infra-common, resources/META-INF/services/org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener

