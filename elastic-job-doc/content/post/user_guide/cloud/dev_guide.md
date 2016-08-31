
+++
date = "2016-01-27T16:14:21+08:00"
title = "开发指南"
weight=11
+++

# 开发指南

## 代码开发

### 作业类型

`Elastic-Job-Lite`和`Elastic-Job-Cloud`提供统一作业接口，开发者仅需对业务作业进行一次开发，之后可根据不同的配置以及部署至不同的`Lite`或`Cloud`环境。

`Elastic-Job`提供`Simple`、`Dataflow`和`Script` `3`种作业类型。
方法参数`shardingContext`包含作业配置、片和运行时信息。可通过`getShardingTotalCount()`, `getShardingItem()`等方法分别获取分片总数，运行在本作业服务器的分片序列号等。

#### 1. Simple类型作业

意为简单实现，未经任何封装的类型。需实现`SimpleJob`接口。该接口仅提供单一方法用于覆盖，此方法将定时执行。与`Quartz`原生接口相似，但增加了弹性扩缩容和分片等功能。

```java
public class MyElasticJob implements SimpleJob {
    
    @Override
    public void process(ShardingContext context) {
        switch (context.getShardingItem()) {
            case 0: 
                // do something by sharding items 0
                break;
            case 1: 
                // do something by sharding items 1
                break;
            case 2: 
                // do something by sharding items 2
                break;
            // case n: ...
        }
    }
}
```

#### 2. Dataflow类型作业

`Dataflow`类型用于处理数据流，需实现`DataflowJob`接口。该接口提供`2`个方法可供覆盖，分别用于抓取(`fetchData`)和处理(`processData`)数据。

```java
public class MyElasticJob implements DataflowElasticJob<Foo> {
    
    @Override
    public List<Foo> fetchData(ShardingContext context) {
        List<Foo> result = new LinkedList<>();
        switch (context.getShardingItem()) {
            case 0: 
                List<Foo> data = // get data from database by sharding items 0
                result.addAll(data);
                break;
            case 1: 
                List<Foo> data = // get data from database by sharding items 1
                result.addAll(data);
                break;
            case 2: 
                List<Foo> data = // get data from database by sharding items 2
                result.addAll(data);
                break;
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

如果采用流式作业处理方式，建议`processData`处理数据后更新其状态，避免`fetchData`再次抓取到，从而使得作业永远不会停止。
流式数据处理参照`TbSchedule`设计，适用于不间歇的数据处理。

#### 3. Script类型作业

`Script`类型作业意为脚本类型作业，支持`shell`，`python`，`perl`等所有类型脚本。只需通过控制台或代码配置`scriptCommandLine`即可，无需编码。执行脚本路径可包含参数，参数传递完毕后，作业框架会自动追加最后一个参数为作业运行时信息。

```
#!/bin/bash
echo sharding execution context is $*
```

作业运行时输出

`sharding execution context is {"shardingItem":[0,1,2,3,4,5,6,7,8,9],"shardingItemParameters":{},"offsets":{},"jobName":"scriptElasticDemoJob","shardingTotalCount":10,"jobParameter":"","monitorExecution":true}`

## 作业配置

`Elastic-Job`配置分为`3`个层级，分别是`Core`, `Type`和`Root`。每个层级使用相似于装饰者模式的方式装配。

`Core`对应`JobCoreConfiguration`，用于提供作业核心配置信息，如：作业名称、分片总数、`CRON`表达式等。

`Type`对应`JobTypeConfiguration`，有`3`个子类分别对应`SIMPLE`, `DATAFLOW`和`SCRIPT`类型作业，提供`3`种作业需要的不同配置，如：`DATAFLOW`类型是否流式处理或`SCRIPT`类型的命令行等。

`Root`对应`JobRootConfiguration`，有`2`个子类分别对应`Lite`和`Cloud`部署类型，提供不同部署类型所需的配置，如：`Lite`类型的是否需要覆盖或`Cloud`占用`CPU`或`Memory`数量等。

### 使用Java代码配置

#### 1. 通用作业配置

```java
    
    // 定义作业核心配置配置
    JobCoreConfiguration simpleCoreConfig = JobCoreConfiguration.newBuilder("demoSimpleJob", "0/15 * * * * ?", 10).build();
    
    // 定义SIMPLE类型配置
    SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(simpleCoreConfig, SimpleDemoJob.class.getCanonicalName());
    
    
    // 定义作业核心配置
    JobCoreConfiguration dataflowCoreConfig = JobCoreConfiguration.newBuilder("demoDataflowJob", "0/30 * * * * ?", 10).build();
        
    // 定义DATAFLOW类型配置
    DataflowJobConfiguration dataflowJobConfig = new DataflowJobConfiguration(dataflowCoreConfig, DataflowDemoJob.class.getCanonicalName(), true);
    
    // 定义作业核心配置配置
    JobCoreConfiguration scriptCoreConfig = JobCoreConfiguration.newBuilder("demoScriptJob", "0/45 * * * * ?", 10).build();
    
    // 定义SCRIPT类型配置
    ScriptJobConfiguration scriptJobConfig = new ScriptJobConfiguration(scriptCoreConfig, "test.sh");
```

#### 2. Cloud作业配置

```java
    JobRootConfiguration jobConfig = new CloudJobConfiguration.newBuilder(simpleJobConfig, cpuCount, memoryMB, appURL);
```

## 作业启动

### 1. Cloud的Java启动方式

需定义`Main`方法并调用`Bootstrap.execute(args)`，例子如下：

```java
public class JobDemo {
    
    public static void main(final String[] args) {
        Bootstrap.execute();
    }
}
```

### 2. Cloud的Spring启动方式

同Java启动方式，但需要通过`REST API`配置bean的名字和Spring配置文件，如{..., "beanName":"simpleJobBean", "applicationContext":"yourDir/applicationContext.xml"}。

之后将作业和用于执行`Java Main`方法的`Shell`脚本打包为`gz.tar`格式，然后使用`Cloud`提供的`REST API`将其部署至`Elastic-Job-Cloud`系统。

## 其他功能

### 异常处理

`elastic-job`在配置中提供了`JobProperties`，可扩展`JobExceptionHandler`接口，并设置`job_exception_handler`定制异常处理流程，默认实现是记录日志但不抛出异常。

### 定制化作业处理线程池

`elastic-job`在配置中提供了`JobProperties`，可扩展`ExecutorServiceHandler`接口，并设置`executor_service_handler`定制线程池。
