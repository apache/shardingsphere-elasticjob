
+++
date = "2016-01-27T16:14:21+08:00"
title = "Elastic-Job-Lite开发指南"
weight=12
+++

# Elastic-Job-Lite开发指南

## 1. 作业开发

`Elastic-Job-Lite`和`Elastic-Job-Cloud`提供统一作业接口，开发者仅需对业务作业进行一次开发，之后可根据不同的配置以及部署至不同的`Lite`或`Cloud`环境。

`Elastic-Job`提供`Simple`、`Dataflow`和`Script` `3`种作业类型。
方法参数`shardingContext`包含作业配置、片和运行时信息。可通过`getShardingTotalCount()`, `getShardingItem()`等方法分别获取分片总数，运行在本作业服务器的分片序列号等。

### a. Simple类型作业

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

### b. Dataflow类型作业

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

### c. Script类型作业

`Script`类型作业意为脚本类型作业，支持`shell`，`python`，`perl`等所有类型脚本。只需通过控制台或代码配置`scriptCommandLine`即可，无需编码。执行脚本路径可包含参数，参数传递完毕后，作业框架会自动追加最后一个参数为作业运行时信息。

```
#!/bin/bash
echo sharding execution context is $*
```

作业运行时输出

`sharding execution context is {"jobName":"scriptElasticDemoJob","shardingTotalCount":10,"jobParameter":"","shardingItem":0,"shardingParameter":"A"}`

## 2. 作业配置

`Elastic-Job`配置分为`3`个层级，分别是`Core`, `Type`和`Root`。每个层级使用相似于装饰者模式的方式装配。

`Core`对应`JobCoreConfiguration`，用于提供作业核心配置信息，如：作业名称、分片总数、`CRON`表达式等。

`Type`对应`JobTypeConfiguration`，有`3`个子类分别对应`SIMPLE`, `DATAFLOW`和`SCRIPT`类型作业，提供`3`种作业需要的不同配置，如：`DATAFLOW`类型是否流式处理或`SCRIPT`类型的命令行等。

`Root`对应`JobRootConfiguration`，有`2`个子类分别对应`Lite`和`Cloud`部署类型，提供不同部署类型所需的配置，如：`Lite`类型的是否需要覆盖本地配置或`Cloud`占用`CPU`或`Memory`数量等。

### a. 使用Java代码配置

**通用作业配置**

```java
    // 定义作业核心配置
    JobCoreConfiguration simpleCoreConfig = JobCoreConfiguration.newBuilder("demoSimpleJob", "0/15 * * * * ?", 10).build();
    // 定义SIMPLE类型配置
    SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(simpleCoreConfig, SimpleDemoJob.class.getCanonicalName());
    // 定义Lite作业根配置
    JobRootConfiguration simpleJobRootConfig = LiteJobConfiguration.newBuilder(simpleJobConfig).build();
    
    // 定义作业核心配置
    JobCoreConfiguration dataflowCoreConfig = JobCoreConfiguration.newBuilder("demoDataflowJob", "0/30 * * * * ?", 10).build();
    // 定义DATAFLOW类型配置
    DataflowJobConfiguration dataflowJobConfig = new DataflowJobConfiguration(dataflowCoreConfig, DataflowDemoJob.class.getCanonicalName(), true);
    // 定义Lite作业根配置
    JobRootConfiguration dataflowJobRootConfig = LiteJobConfiguration.newBuilder(dataflowJobConfig).build();
    
    // 定义作业核心配置配置
    JobCoreConfiguration scriptCoreConfig = JobCoreConfiguration.newBuilder("demoScriptJob", "0/45 * * * * ?", 10).build();
    // 定义SCRIPT类型配置
    ScriptJobConfiguration scriptJobConfig = new ScriptJobConfiguration(scriptCoreConfig, "test.sh");
    // 定义Lite作业根配置
    JobRootConfiguration scriptJobRootConfig = LiteJobConfiguration.newBuilder(scriptCoreConfig).build();
```

### b. Spring命名空间配置

与`Spring`容器配合使用作业，可将作业`Bean`配置为`Spring Bean`，并在作业中通过依赖注入使用`Spring`容器管理的数据源等对象。可用`placeholder`占位符从属性文件中取值。`Lite`可考虑使用`Spring`命名空间方式简化配置。

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
    
    <!-- 配置简单作业-->
    <job:simple id="simpleElasticJob" class="xxx.MySimpleElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
    
    <!-- 配置数据流作业-->
    <job:dataflow id="throughputDataflow" class="xxx.MyThroughputDataflowElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
    
    <!-- 配置脚本作业-->
    <job:script id="scriptElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" script-command-line="/your/file/path/demo.sh" />
    
    <!-- 配置带监听的简单作业-->
    <job:simple id="listenerElasticJob" class="xxx.MySimpleListenerElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C">
        <job:listener class="xx.MySimpleJobListener"/>
        <job:distributed-listener class="xx.MyOnceSimpleJobListener" started-timeout-milliseconds="1000" completed-timeout-milliseconds="2000" />
    </job:simple>
    
    <!-- 配置带作业数据库事件追踪的简单作业-->
    <job:simple id="listenerElasticJob" class="xxx.MySimpleListenerElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" event-trace-rdb-data-source="yourDataSource">
    </job:simple>
</beans>
```

配置项详细说明请参见[Elastic-Job-Lite配置手册](../config_manual/)

## 3. 作业启动

### a. Java启动方式

```java
public class JobDemo {
    
    public static void main(String[] args) {
        new JobScheduler(createRegistryCenter(), createJobConfiguration()).init();
    }
    
    private static CoordinatorRegistryCenter createRegistryCenter() {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration("zk_host:2181", "elastic-job-demo"));
        regCenter.init();
        return regCenter;
    }
    
    private static LiteJobConfiguration createJobConfiguration() {
        // 创建作业配置
        ...
    }
}
```

### b. Spring启动方式

将配置`Spring`命名空间的xml通过`Spring`启动，作业将自动加载。
