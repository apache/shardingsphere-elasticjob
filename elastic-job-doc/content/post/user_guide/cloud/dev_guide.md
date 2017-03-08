+++
date = "2016-01-27T16:14:21+08:00"
title = "Elastic-Job-Cloud开发指南"
weight=56
+++

# Elastic-Job-Cloud开发指南

## 作业开发

`Elastic-Job-Lite`和`Elastic-Job-Cloud`提供统一作业接口，开发者仅需对业务作业进行一次开发，之后可根据不同的配置以及部署至不同的`Lite`或`Cloud`环境。

`Elastic-Job`提供`Simple`、`Dataflow`和`Script` `3`种作业类型。
方法参数`shardingContext`包含作业配置、片和运行时信息。可通过`getShardingTotalCount()`, `getShardingItem()`等方法分别获取分片总数，运行在本作业服务器的分片序列号等。

### 1. Simple类型作业

意为简单实现，未经任何封装的类型。需实现`SimpleJob`接口。该接口仅提供单一方法用于覆盖，此方法将定时执行。与`Quartz`原生接口相似，但提供了弹性扩缩容和分片等功能。

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

### 2. Dataflow类型作业

`Dataflow`类型用于处理数据流，需实现`DataflowJob`接口。该接口提供`2`个方法可供覆盖，分别用于抓取(`fetchData`)和处理(`processData`)数据。

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
        return result;
    }
    
    @Override
    public void processData(ShardingContext shardingContext, List<Foo> data) {
        // process data
        // ...
    }
}
```

***

**流式处理**

可通过`DataflowJobConfiguration`配置是否流式处理。

流式处理数据只有`fetchData`方法的返回值为`null`或集合长度为空时，作业才停止抓取，否则作业将一直运行下去；
非流式处理数据则只会在每次作业执行过程中执行一次`fetchData`方法和`processData`方法，随即完成本次作业。

如果采用流式作业处理方式，建议`processData`处理数据后更新其状态，避免`fetchData`再次抓取到，从而使得作业永不停止。
流式数据处理参照`TbSchedule`设计，适用于不间歇的数据处理。

### 3. Script类型作业

`Script`类型作业意为脚本类型作业，支持`shell`，`python`，`perl`等所有类型脚本。只需通过控制台或代码配置`scriptCommandLine`即可，无需编码。执行脚本路径可包含参数，参数传递完毕后，作业框架会自动追加最后一个参数为作业运行时信息。

```
#!/bin/bash
echo sharding execution context is $*
```

作业运行时输出

`sharding execution context is {"jobName":"scriptElasticDemoJob","shardingTotalCount":10,"jobParameter":"","shardingItem":0,"shardingParameter":"A"}`

## 作业启动

### 1. Java启动方式

需定义`Main`方法并调用`JobBootstrap.execute()`，例子如下：

```java
public class JobDemo {
    
    public static void main(final String[] args) {
        JobBootstrap.execute();
    }
}
```

### 2. Spring启动方式

同`Java`启动方式，但需要通过`REST API`配置`bean`的名字和`Spring`配置文件位置，如：

```json
{..., "beanName":"simpleJobBean", "applicationContext":"yourDir/applicationContext.xml"}
```

之后将作业和用于执行`Java Main`方法的`Shell`脚本打包为`gz.tar`格式，然后使用`Cloud`提供的`REST API`将其部署至`Elastic-Job-Cloud`系统。如对如何打包不理解请参考我们提供的`example`。

## 其他功能

### 1. 作业事件追踪
作业事件追踪目前支持数据库方式配置。

更多信息请参见[Elastic-Job事件追踪](../../common/event_trace/)。

### 2. 异常处理

`elastic-job`在配置中提供了`JobProperties`，可扩展`JobExceptionHandler`接口，并设置`job_exception_handler`定制异常处理流程。默认实现是记录日志但不抛出异常。

### 3. 定制化作业处理线程池

`elastic-job`在配置中提供了`JobProperties`，可扩展`ExecutorServiceHandler`接口，并设置`executor_service_handler`定制线程池。默认使用`CPU*2`线程数的线程池。
