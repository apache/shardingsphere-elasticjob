
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
方法参数`shardingContext`包含作业配置、片和运行时信息。可通过`getShardingTotalCount()`, `getShardingItems()`等方法分别获取分片总数，运行在本作业服务器的分片序列号集合等。

#### 1. Simple类型作业

意为简单实现，未经任何封装的类型。需实现`SimpleJob`接口。该接口仅提供单一方法用于覆盖，此方法将定时执行。与`Quartz`原生接口相似，但增加了弹性扩缩容和分片等功能。

```java
public class MyElasticJob implements SimpleJob {
    
    @Override
    public void execute(ShardingContext shardingContext) {
        // do something by sharding items
    }
}
```

#### 2. Dataflow类型作业
`Dataflow`类型用于处理数据流，需实现`DataflowJob`接口。该接口提供`2`个方法可供覆盖，分别用于抓取(`fetchData`)和处理(`processData`)数据。

```java
public class MyElasticJob extends AbstractIndividualThroughputDataflowElasticJob<Foo> {
    
    @Override
    public List<T> fetchData(ShardingContext shardingContext) {
        List<Foo> result = // get data from database by sharding items
        return result;
    }
    
    @Override
    public void processData(ShardingContext shardingContext, List<T> data) {
        // process data
        // ...
    }
}
```

***

**处理方式**

`Dataflow`作业提供`2`种作业类型，分别是`THROUGHPUT`和`SEQUENCE`。

`THROUGHPUT`意为高吞吐的数据流作业，可将获取到的数据多线程处理，但不会保证多线程处理数据的顺序。
`SEQUENCE`为每一分片项分配一个线程，可保证同一分片下数据处理的正确性。

如：
`THROUGHPUT`类型从`2`个分片共获取到`100`条数据，第`1`个分片`40`条，第`2`个分片`60`条，配置为`2`个线程处理，则第`1`个线程处理前`50`条数据，第`2`个线程处理后`50`条数据，无视分片项；
`SEQUENCE`类型作业则根据当前服务器所分配的分片项数量进行多线程处理，每个分片项使用同一线程处理，防止了同一分片的数据被多线程处理，从而导致的顺序问题。
如：从`2`个分片共获取到`100`条数据，第`1`个分片`40`条，第`2`个分片`60`条，则系统自动分配两个线程处理，第`1`个线程处理第`1`个分片的`40`条数据，第`2`个线程处理第`2`个分片的`60`条数据。
由于`THROUGHPUT`作业可以使用多于分片项的任意线程数处理，所以性能可能会优于`SEQUENCE`作业。

**流式处理**

可通过`JobConfiguration`配置是否流式处理。

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

`sharding execution context is {"shardingItems":[0,1,2,3,4,5,6,7,8,9],"shardingItemParameters":{},"offsets":{},"jobName":"scriptElasticDemoJob","shardingTotalCount":10,"jobParameter":"","monitorExecution":true}`

## 作业配置

`Elastic-Job`配置分为`3`个层级，分别是`Core`, `Type`和`Root`。每个层级使用相似于装饰者模式的方式装配。

`Core`类型对应`JobCoreConfiguration`，用于提供作业核心配置信息，如：作业名称、分片总数、`CRON`表达式等。

`Type`类型对应`JobTypeConfiguration`，有`3`个子类分别对应`SIMPLE`, `DATAFLOW`和`SCRIPT`类型作业，提供`3`种作业需要的不同配置，如：`DATAFLOW`类型的作业处理方式、是否流式处理或`SCRIPT`类型的命令行等。

`Root`类型对应`JobRootConfiguration`，有`2`个子类分别对应`Lite`和`Cloud`部署类型，提供不同部署类型所需的配置，如：`Lite`类型的是否需要覆盖或`Cloud`占用`CPU`或`Memory`数量等。


### 使用Java代码配置

#### 1. 通用作业配置

```java
    
    // 定义作业核心配置配置
    JobCoreConfiguration simpleCoreConfig = JobCoreConfiguration.newBuilder("demoSimpleJob", "0/30 * * * * ?", 10).build();
    
    // 定义SIMPLE类型
    SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(simpleCoreConfig, SimpleDemoJob.class.getCanonicalName());
    
    
    // 定义作业核心配置
    JobCoreConfiguration dataflowCoreConfig = JobCoreConfiguration.newBuilder("demoDataflowJob", "0/30 * * * * ?", 10).build();
        
    // 定义DATAFLOW类型配置
    DataflowJobConfiguration dataflowJobConfig = new DataflowJobConfiguration(dataflowCoreConfig, DataflowDemoJob.class.getCanonicalName(), DataflowJobConfiguration.DataflowType.THROUGHPUT, true);
    
    
    // 定义作业核心配置配置
    JobCoreConfiguration scriptCoreConfig = JobCoreConfiguration.newBuilder("demoScriptJob", "0/30 * * * * ?", 10).build();
    
    // 定义SCRIPT类型配置
    ScriptJobConfiguration scriptJobConfig = new ScriptJobConfiguration(scriptCoreConfig, "test.sh");
```

#### 2. Lite作业配置

```java
    JobRootConfiguration jobConfig = LiteJobConfiguration.newBuilder(simpleJobConfig).build();
```

#### 3. Cloud作业配置

```java
    // dockerImageName暂时保留, 还未实现
    JobRootConfiguration jobConfig = new CloudJobConfiguration.newBuilder(simpleJobConfig, cpuCount, memoryMB, dockerImageName, appURL);
```

### Spring命名空间配置

与`Spring`容器配合使用作业，可将作业`Bean`配置为`Spring Bean`，并在作业中通过依赖注入使用`Spring`容器管理的数据源等对象。可用`placeholder`占位符从属性文件中取值。目前仅提供`Lite`的`Spring`命名空间。

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
    <job:simple id="simpleElasticJob" class="xxx.MySimpleElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?"   sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
    
    <!-- 配置数据流作业-->
    <job:dataflow id="throughputDataflow" class="xxx.MyThroughputDataflowElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" process-count-interval-seconds="10" concurrent-data-process-thread-count="10" />
    
    <!-- 配置脚本作业-->
    <job:script id="scriptElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" script-command-line="/your/file/path/demo.sh" />
    
    <!-- 配置带监听的简单作业-->
    <job:simple id="listenerElasticJob" class="xxx.MySimpleListenerElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?"   sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C">
        <job:listener class="xx.MySimpleJobListener"/>
        <job:listener class="xx.MyOnceSimpleJobListener" started-timeout-milliseconds="1000" completed-timeout-milliseconds="2000" />
    </job:simple>
</beans>
```

#### job:simple命名空间属性详细说明

| 属性名                              | 类型  |是否必填 |缺省值| 描述                                                                       |
| -----------------------------------|:------|:-------|:----|:---------------------------------------------------------------------------|
|id                                  |String |`是`    |     | 作业名称                                                                    |
|class                               |String |否      |     | 作业实现类，需实现`ElasticJob`接口，脚本型作业不需要配置                         |
|registry-center-ref                 |String |`是`    |     | 注册中心`Bean`的引用，需引用`reg:zookeeper`的声明                              |
|cron                                |String |`是`    |     | `cron`表达式，用于配置作业触发时间                                             |
|sharding-total-count                |int    |`是`    |     | 作业分片总数                                                                 |
|sharding-item-parameters            |String |否      |     | 分片序列号和参数用等号分隔，多个键值对用逗号分隔<br />分片序列号从`0`开始，不可大于或等于作业分片总数<br />如：<br/>`0=a,1=b,2=c`|
|job-parameter                       |String |否      |     | 作业自定义参数<br />作业自定义参数，可通过传递该参数为作业调度的业务方法传参，用于实现带参数的作业<br />例：每次获取的数据量、作业实例从数据库读取的主键等 |
|monitor-execution                   |boolean|否      |true | 监控作业运行时状态<br />每次作业执行时间和间隔时间均非常短的情况，建议不监控作业运行时状态以提升效率。因为是瞬时状态，所以无必要监控。请用户自行增加数据堆积监控。并且不能保证数据重复选取，应在作业中实现幂等性。<br />每次作业执行时间和间隔时间均较长的情况，建议监控作业运行时状态，可保证数据不会重复选取。|
|monitor-port                        |int    |否      |-1   | 作业监控端口<br />建议配置作业监控端口, 方便开发者dump作业信息。<br />使用方法: echo "dump" \| nc 127.0.0.1 9888|
|max-time-diff-seconds               |int    |否      |-1   | 最大允许的本机与注册中心的时间误差秒数<br />如果时间误差超过配置秒数则作业启动时将抛异常<br />配置为`-1`表示不校验时间误差|
|failover                            |boolean|否      |false| 是否开启失效转移<br />仅`monitorExecution`开启，失效转移才有效                   |
|misfire                             |boolean|否      |true | 是否开启错过任务重新执行                                                       |
|job-sharding-strategy-class         |String |否      |true | 作业分片策略实现类全路径<br />默认使用平均分配策略<br />详情参见：[作业分片策略](http://dangdangdotcom.github.io/elastic-job/post/job_strategy)  |
|description                         |String |否      |     | 作业描述信息                                                                 |
|disabled                            |boolean|否      |false| 作业是否禁止启动<br />可用于部署作业时，先禁止启动，部署结束后统一启动              |
|overwrite                           |boolean|否      |false| 本地配置是否可覆盖注册中心配置<br />如果可覆盖，每次启动作业都以本地配置为准         |
|jobProperties                       |String |否      |     | 作业定制化属性，目前支持`job_exception_handler`和`executor_service_handler`，用于扩展异常处理和自定义作业处理线程池 |

#### job:dataflow命名空间属性详细说明

job:dataflow命名空间拥有job:simple命名空间的全部属性，以下仅列出特有属性

| 属性名                              | 类型  |是否必填 |缺省值| 描述                                                                                                                         |
| ---------------------------------- |:------|:-------|:--------|:------------------------------------------------------------------------------------------------------------------------|
|concurrent-data-process-thread-count|int    |否      |CPU核数*2 | 同时处理数据的并发线程数<br />不能小于1<br />仅`ThroughputDataflow`作业有效                                                   |
|streaming-process                   |boolean|否      |false    | 是否流式处理数据<br />如果流式处理数据, 则`fetchData`不返回空结果将持续执行作业<br />如果非流式处理数据, 则处理数据完成后作业结束<br />|

#### job:script命名空间属性详细说明，基本属性参照job:simple命名空间属性详细说明

job:script命名空间拥有job:simple命名空间的全部属性，以下仅列出特有属性

| 属性名                            | 类型  |是否必填 |缺省值| 描述                                                                       |
| -------------------------------- |:------|:-------|:----|:---------------------------------------------------------------------------|
|script-command-line               |String |否      |     | 脚本型作业执行命令行                                                          |

#### job:listener命名空间属性详细说明

`job:listener`必须配置为`job:bean`的子元素

| 属性名                          | 类型  |是否必填|缺省值         | 描述                                                                                             |
| ------------------------------ |:------|:------|:-------------|:------------------------------------------------------------------------------------------------|
|class                           |String |`是`   |              | 前置后置任务监听实现类，需实现`ElasticJobListener`接口                                               |
|started-timeout-milliseconds    |long   |`否`   |Long.MAX_VALUE| AbstractDistributeOnceElasticJobListener型监听器，最后一个作业执行前的执行方法的超时时间<br />单位：毫秒|
|completed-timeout-milliseconds  |long   |`否`   |Long.MAX_VALUE| AbstractDistributeOnceElasticJobListener型监听器，最后一个作业执行后的执行方法的超时时间<br />单位：毫秒|

#### reg:bean命名空间属性详细说明

| 属性名                          |类型   |是否必填|缺省值|描述                                                                                                |
| ------------------------------ |:------|:------|:----|:--------------------------------------------------------------------------------------------------|
|id                              |String |`是`   |     | 注册中心在`Spring`容器中的主键                                                                        |
|server-lists                    |String |`是`   |     | 连接`Zookeeper`服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: host1:2181,host2:2181|
|namespace                       |String |`是`   |     | `Zookeeper`的命名空间                                                                               |
|base-sleep-time-milliseconds    |int    |否     |1000 | 等待重试的间隔时间的初始值<br />单位：毫秒                                                              |
|max-sleep-time-milliseconds     |int    |否     |3000 | 等待重试的间隔时间的最大值<br />单位：毫秒                                                              |
|max-retries                     |int    |否     |3    | 最大重试次数                                                                                        |
|session-timeout-milliseconds    |int    |否     |60000| 会话超时时间<br />单位：毫秒                                                                          |
|connection-timeout-milliseconds |int    |否     |15000| 连接超时时间<br />单位：毫秒                                                                          |
|digest                          |String |否     |无验证| 连接`Zookeeper`的权限令牌<br />缺省为不需要权限验证                                                     |
|nested-port                     |int    |否     |-1   | 内嵌`Zookeeper`的端口号<br />-1表示不开启内嵌`Zookeeper`                                               |
|nested-data-dir                 |String |否     |     | 内嵌`Zookeeper`的数据存储路径<br />为空表示不开启内嵌`Zookeeper`                                        |
```

## 作业启动

### 1. Lite的Java启动方式

```java

public class JobDemo {
    
    public static void main(final String[] args) {
        new JobDemo().init();
    }
    
    private void init() {
        // 连接注册中心
        regCenter.init();
        // 启动作业
        new JobScheduler(regCenter, jobRootConfig).init();
    }
}
```

### 2. Lite的Spring启动方式

参见`Spring`命名空间

### 3. Cloud启动方式

需定义`Main`方法并调用`Bootstrap.execute(args)`，例子如下：

```java

public class JobDemo {
    
    public static void main(final String[] args) {
        Bootstrap.execute(args);
    }
}
```

之后将作业和用于执行`Java Main`方法的`Shell`脚本打包为`gz.tar`格式，然后使用`Cloud`提供的`REST API`将其部署至`Elastic-Job-Cloud`系统。

## 其他功能

### 异常处理

`elastic-job`在配置中提供了`JobProperties`，可扩展`JobExceptionHandler`接口，并设置`job_exception_handler`定制异常处理流程，默认实现是记录日志但不抛出异常。

### 定制化作业处理线程池

`elastic-job`在配置中提供了`JobProperties`，可扩展`ExecutorServiceHandler`接口，并设置`executor_service_handler`定制线程池。

### 任务监听
可通过配置多个任务监听器，在任务执行前和执行后执行监听的方法。监听器分为每台作业节点均执行和分布式场景中仅单一节点执行`2`种。

#### 1. 每台作业节点均执行的监听
若作业处理作业服务器的文件，处理完成后删除文件，可考虑使用每个节点均执行清理任务。此类型任务实现简单，且无需考虑全局分布式任务是否完成，请尽量使用此类型监听器。

步骤：

* 定义监听器

```java
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;

public class MyElasticJobListener implements ElasticJobListener {
    
    @Override
    public void beforeJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
        // do something ...
    }
    
    @Override
    public void afterJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
        // do something ...
    }
}
```

* 将监听器作为参数传入`JobScheduler`

```java
public class JobMain {
    
    public static void main(final String[] args) {
        new JobScheduler(regCenter, jobConfig, new MyElasticJobListener()).init();    
    }
}
```

#### 2. 分布式场景中仅单一节点执行的监听
若作业处理数据库数据，处理完成后只需一个节点完成数据清理任务即可。此类型任务处理复杂，需同步分布式环境下作业的状态同步，提供了超时设置来避免作业不同步导致的死锁，请谨慎使用。

步骤：

* 定义监听器

```java
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;

public final class TestDistributeOnceElasticJobListener extends AbstractDistributeOnceElasticJobListener {
    
    public TestDistributeOnceElasticJobListener(final long startTimeoutMills, final long completeTimeoutMills) {
        super(startTimeoutMills, completeTimeoutMills);
    }
    
    @Override
    public void doBeforeJobExecutedAtLastStarted(final JobExecutionMultipleShardingContext shardingContext) {
        // do something ...
    }
    
    @Override
    public void doAfterJobExecutedAtLastCompleted(final JobExecutionMultipleShardingContext shardingContext) {
        // do something ...
    }
}
```

* 将监听器作为参数传入`JobScheduler`

```java
public class JobMain {

    public static void main(final String[] args) {
        long startTimeoutMills = 5000L;
        long completeTimeoutMills = 10000L;    
        new JobScheduler(regCenter, jobConfig, new MyDistributeOnceElasticJobListener(startTimeoutMills, completeTimeoutMills)).init();
    }
}
```
