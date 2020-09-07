+++
title = "Job Development"
weight = 1
chapter = true
+++

ElasticJob-Lite and ElasticJob-Cloud provide a unified job interface, developers need to develop business jobs only once, and then they can be deployed to different environments according to different configurations and deployments.

ElasticJob has two kinds of job types: Class-based job and Type-based job.
Class-based jobs require developers to weave business logic by implementing interfaces;
Type-based jobs don't need coding, just need to provide the corresponding configuration.

The method parameter `shardingContext` of the class-based job interface contains job configuration, slice and runtime information.
Through methods such as `getShardingTotalCount()`, `getShardingItem()`, user can obtain the total number of shards, the serial number of the shards running on the job server, etc.

ElasticJob provides two class-based job types which are `Simple` and `Dataflow`; and also provides a type-based job which is `Script`. Users can extend job types by implementing the SPI interface.

## Simple Job

It means simple implementation, without any encapsulation type. Need to implement `SimpleJob` interface.
This interface only provides a single method for coverage, and this method will be executed periodically.
It is similar to Quartz's native interface, but provides functions such as elastic scaling and slice.

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

## Dataflow Job

For processing data flow, need to implement `DataflowJob` interface.
This interface provides two methods for coverage, which are used to fetch (fetchData) and process (processData) data.

```java
public class MyElasticJob implements DataflowJob<Foo> {
    
    @Override
    public List<Foo> fetchData(ShardingContext context) {
        switch (context.getShardingItem()) {
            case 0: 
                List<Foo> data = // get data from database by sharding item 0
                return data;
            case 1: 
                List<Foo> data = // get data from database by sharding item 1
                return data;
            case 2: 
                List<Foo> data = // get data from database by sharding item 2
                return data;
            // case n: ...
        }
    }
    
    @Override
    public void processData(ShardingContext shardingContext, List<Foo> data) {
        // process data
        // ...
    }
}
```

***

**Streaming**

Streaming can be enabled or disabled through the property `streaming.process`.

If streaming is enabled, the job will stop fetching data only when the return value of the `fetchData` method is null or the collection is empty, otherwise the job will continue to run;
If streaming is disabled, the job will execute the `fetchData` and `processData` methods only once during each job execution, and then the job will be completed immediately.

If use the streaming job to process data, it is recommended to update its status after the `processData` method being executed, to avoid being fetched again by the method `fetchData`, so that the job never stops.

## Script job

Support all types of scripts such as `shell`, `python`, `perl`.
The script to be executed can be configured through the property `script.command.line`, without coding.
The script path can contain parameters, after the parameters are passed, the job framework will automatically append the last parameter as the job runtime information.

The script example is as follows:

```bash
#!/bin/bash
echo sharding execution context is $*
```

When the job runs, it will output:

```
sharding execution context is {"jobName":"scriptElasticDemoJob","shardingTotalCount":10,"jobParameter":"","shardingItem":0,"shardingParameter":"A"}
```

## HTTP job (Since 3.0.0-beta)

The http information to be requested can be configured through the properties of `http.url`, `http.method`, `http.data`, etc.
Sharding information is transmitted in the form of Header, the key is `shardingContext`, and the value is in json format.

```java

public class HttpJobMain {
    
    public static void main(String[] args) {
        
        new ScheduleJobBootstrap(regCenter, "HTTP", JobConfiguration.newBuilder("javaHttpJob", 1)
                .setProperty(HttpJobProperties.URI_KEY, "http://xxx.com/execute")
                .setProperty(HttpJobProperties.METHOD_KEY, "POST")
                .setProperty(HttpJobProperties.DATA_KEY, "source=ejob")
                .cron("0/5 * * * * ?").shardingItemParameters("0=Beijing").build()).schedule();
    }
}
```
```java
@Controller
@Slf4j
public class HttpJobController {
    
    @RequestMapping(path = "/execute", method = RequestMethod.POST)
    public void execute(String source, @RequestHeader String shardingContext) {
        log.info("execute from source : {}, shardingContext : {}", source, shardingContext);
    }
}
```

When the job runs, it will output：
```
execute from source : ejob, shardingContext : {"jobName":"scriptElasticDemoJob","shardingTotalCount":3,"jobParameter":"","shardingItem":0,"shardingParameter":"Beijing"}
```
