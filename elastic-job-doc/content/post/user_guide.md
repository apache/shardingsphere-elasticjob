+++
date = "2016-01-27T16:14:21+08:00"
title = "开发指南"
weight=6
+++

# 开发指南

## 代码开发

### 作业类型

目前提供`2`种作业类型，分别是`Simple`和`DataFlow`。`DataFlow`类型用于处理数据流，它又提供`2`种作业类型，分别是`ThroughputDataFlow`和`SequenceDataFlow`。需要继承相应的抽象类。

方法参数`shardingContext`包含作业配置，分片和运行时信息。可通过`getShardingTotalCount()`, `getShardingItems()`等方法分别获取分片总数，运行在本作业服务器的分片序列号集合等。

#### Simple类型作业

`Simple`类型作业意为简单实现，未经任何封装的类型。需要继承`AbstractSimpleElasticJob`，该类只提供了一个方法用于覆盖，此方法将被定时执行。用于执行普通的定时任务，与`Quartz`原生接口相似，只是增加了弹性扩缩容和分片等功能。

```java
public class MyElasticJob extends AbstractSimpleElasticJob {
    
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // do something by sharding items
    }
}
```

#### ThroughputDataFlow类型作业

`ThroughputDataFlow`类型作业意为高吞吐的数据流作业。需要继承`AbstractThroughputDataFlowElasticJob`并可以指定返回值泛型，该类提供`3`个方法可覆盖，分别用于抓取数据，处理数据和指定是否流式处理数据。可以获取数据处理成功失败次数等辅助监控信息。如果流式处理数据，`fetchData`方法的返回值只有为`null`或长度为空时，作业才会停止执行，否则作业会一直运行下去；非流式处理数据则只会在每次作业执行过程中执行一次`fetchData`方法和`processData`方法，即完成本次作业。流式数据处理参照`TbSchedule`设计，适用于不间歇的数据处理。

作业执行时会将`fetchData`的数据传递给`processData`处理，其中`processData`得到的数据是通过多线程（线程池大小可配）拆分的。如果采用流式作业处理方式，建议`processData`处理数据后更新其状态，避免`fetchData`再次抓取到，从而使得作业永远不会停止。`processData`的返回值用于表示数据是否处理成功，抛出异常或者返回`false`将会在统计信息中归入失败次数，返回`true`则归入成功次数。

```java
public class MyElasticJob extends AbstractThroughputDataFlowElasticJob<Foo> {
    
    @Override
    public List<Foo> fetchData(JobExecutionMultipleShardingContext context) {
        Map<Integer, String> offset = context.getOffsets();
        List<Foo> result = // get data from database by sharding items and by offset
        return result;
    }
    
    @Override
    public boolean processData(JobExecutionMultipleShardingContext context, Foo data) {
        // process data
        // ...
        
        // store offset
        for (int each : context.getShardingItems()) {
            updateOffset(each, "your offset, maybe id");
        }
        return true;
    }
    
    @Override
    public boolean isStreamingProcess() {
        return true;
    }
}
```

#### SequenceDataFlow类型作业

`SequenceDataFlow`类型作业和`ThroughputDataFlow`作业类型极为相似，所不同的是`ThroughputDataFlow`作业类型可以将获取到的数据多线程处理，但不会保证多线程处理数据的顺序。如：从`2`个分片共获取到`100`条数据，第`1`个分片`40`条，第`2`个分片`60`条，配置为两个线程处理，则第`1`个线程处理前`50`条数据，第`2`个线程处理后`50`条数据，无视分片项；`SequenceDataFlow`类型作业则根据当前服务器所分配的分片项数量进行多线程处理，每个分片项使用同一线程处理，防止了同一分片的数据被多线程处理，从而导致的顺序问题。如：从`2`个分片共获取到`100`条数据，第`1`个分片`40`条，第`2`个分片`60`条，则系统自动分配两个线程处理，第`1`个线程处理第`1`个分片的`40`条数据，第`2`个线程处理第`2`个分片的`60`条数据。由于`ThroughputDataFlow`作业可以使用多于分片项的任意线程数处理，所以性能调优的可能会优于`SequenceDataFlow`作业。

```java
public class MyElasticJob extends AbstractIndividualSequenceDataFlowElasticJob<Foo> {
    
    @Override
    public List<Foo> fetchData(JobExecutionSingleShardingContext context) {
        int offset = context.getOffset();
        List<Foo> result = // get data from database by sharding items and by offset
        return result;
    }
    
    @Override
    public boolean processData(JobExecutionSingleShardingContext context, Foo data) {
        // process data
        // ...
        
        // store offset
        updateOffset(context.getShardingItem(), "your offset, maybe id");
        return true;
    }
    
    @Override
    public boolean isStreamingProcess() {
        return true;
    }
}
```

### 批量处理
为了提高数据处理效率，数据流类型作业提供了批量处理数据的功能。之前逐条处理数据的两个抽象类分别是`AbstractIndividualThroughputDataFlowElasticJob`和`AbstractIndividualSequenceDataFlowElasticJob`，批量处理则使用另外两个接口`AbstractBatchThroughputDataFlowElasticJob`和`AbstractBatchSequenceDataFlowElasticJob`。不同之处在于`processData`方法的返回值从`boolean`类型变为`int`类型，用于表示一批数据处理的成功数量，第二个入参则转变为`List`数据集合。

### 异常处理

`elastic-job`在最上层接口提供了`handleJobExecutionException`方法，使用作业时可以覆盖此方法，并使用`quartz`提供的`JobExecutionException`控制异常后作业的声明周期。默认实现是直接将异常抛出。示例：

```java
public class XXXSimpleJob extends AbstractSimpleElasticJob {
    
    @Override
    public void process(final JobExecutionMultipleShardingContext context) {
        int zero = 0;
        10 / zero;
    }
    
    @Override
    public void handleJobExecutionException(JobExecutionException jobExecutionException) throws JobExecutionException {
        jobExecutionException.unscheduleAllTriggers();
    }
}

```

### 任务监听配置
可以通过配置多个任务监听器，在任务执行前和执行后执行监听的方法。监听器分为每台作业节点均执行和分布式场景中仅单一节点执行两种。

#### 每台作业节点均执行的监听
若作业处理作业服务器的文件，处理完成后删除文件，可考虑使用每个节点均执行清理任务。此类型任务实现简单，且无需考虑全局分布式任务是否完成，请尽量使用此类型监听器。

步骤：

* 定义监听器

```java
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;

public class MyElasticJobListener implements AbstractDistributeOnceElasticJobListener {
    
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

#### 分布式场景中仅单一节点执行的监听
若作业处理数据库数据，处理完成后只需一个节点完成数据清理任务即可。此类型任务处理复杂，需同步分布式环境下作业的状态同步，提供了超时设置来避免作业不同步导致的死锁，请谨慎使用。

步骤：

* 定义监听器

```java
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;

public final class TestDistributeOnceElasticJobListener extends AbstractDistributeOnceElasticJobListener {
    
    public MyDistributeOnceElasticJobListener(final long startTimeoutMills, final long completeTimeoutMills) {
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

## 作业配置

与`Spring`容器配合使用作业，可以将作业`Bean`配置为`Spring Bean`，可在作业中通过依赖注入使用`Spring`容器管理的数据源等对象。可用`placeholder`占位符从属性文件中取值。

### Spring命名空间配置

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
    <reg:zookeeper id="regCenter" serverLists=" yourhost:2181" namespace="dd-job" baseSleepTimeMilliseconds="1000" maxSleepTimeMilliseconds="3000" maxRetries="3" />
    
    <!-- 配置作业A-->
    <job:bean id="simpleElasticJob" class="xxx.MySimpleElasticJob" regCenter="regCenter" cron="0/10 * * * * ?"   shardingTotalCount="3" shardingItemParameters="0=A,1=B,2=C" />
    
    <!-- 配置作业B-->
    <job:bean id="throughputDataFlow" class="xxx.MyThroughputDataFlowElasticJob" regCenter="regCenter" cron="0/10 * * * * ?" shardingTotalCount="3" shardingItemParameters="0=A,1=B,2=C" processCountIntervalSeconds="10" concurrentDataProcessThreadCount="10" />
    
    <!-- 配置作业C-->
    <job:bean id="listenerElasticJob" class="xxx.MySimpleListenerElasticJob" regCenter="regCenter" cron="0/10 * * * * ?"   shardingTotalCount="3" shardingItemParameters="0=A,1=B,2=C">
        <job:listener class="xx.MySimpleJobListener"/>
        <job:listener class="xx.MyOnceSimpleJobListener" startedTimeoutMilliseconds="1000" completedTimeoutMilliseconds="2000" />
    </job:bean>
</beans>
```

#### job:bean命名空间属性详细说明

| 属性名                          | 类型  |是否必填 |缺省值| 描述                                                                       |
| ------------------------------ |:------|:-------|:----|:---------------------------------------------------------------------------|
|id                              |String |`是`    |     | 作业名称                                                                    |
|class                           |String |`是`    |     | 作业实现类，需实现`ElasticJob`接口                                            |
|regCenter                       |String |`是`    |     | 注册中心`Bean`的引用，需引用`reg:zookeeper`的声明                              |
|cron                            |String |`是`    |     | `cron`表达式，用于配置作业触发时间                                             |
|shardingTotalCount              |int    |`是`    |     | 作业分片总数                                                                 |
|shardingItemParameters          |String |否      |     | 分片序列号和参数用等号分隔，多个键值对用逗号分隔<br />分片序列号从`0`开始，不可大于或等于作业分片总数<br />如：<br/>`0=a,1=b,2=c`|
|jobParameter                    |String |否      |     | 作业自定义参数<br />可以配置多个相同的作业，但是用不同的参数作为不同的调度实例     |
|monitorExecution                |boolean|否      |true | 监控作业运行时状态<br />每次作业执行时间和间隔时间均非常短的情况，建议不监控作业运行时状态以提升效率。因为是瞬时状态，所以无必要监控。请用户自行增加数据堆积监控。并且不能保证数据重复选取，应在作业中实现幂等性。<br />每次作业执行时间和间隔时间均较长的情况，建议监控作业运行时状态，可保证数据不会重复选取。|
|monitorPort                     |int    |否      |-1   | 作业监控端口。<br />建议配置作业监控端口, 方便开发者dump作业信息。<br />使用方法: echo "dump" \| nc 127.0.0.1 9888|
|processCountIntervalSeconds     |int    |否      |300  | 统计作业处理数据数量的间隔时间<br />单位：秒<br />仅对`DataFlow`类型作业有效       |
|concurrentDataProcessThreadCount|int    |否      |1    | 同时处理数据的并发线程数<br />不能小于1<br />仅`ThroughputDataFlow`作业有效      |
|fetchDataCount                  |int    |否      |0    | 每次抓取的数据量                                                              |
|maxTimeDiffSeconds              |int    |否      |-1   | 最大允许的本机与注册中心的时间误差秒数<br />如果时间误差超过配置秒数则作业启动时将抛异常<br />配置为`-1`表示不校验时间误差|
|failover                        |boolean|否      |false| 是否开启失效转移<br />仅`monitorExecution`开启，失效转移才有效                   |
|misfire                         |boolean|否      |true | 是否开启错过任务重新执行                                                       |
|jobShardingStrategyClass        |String |否      |true | 作业分片策略实现类全路径<br />默认使用平均分配策略<br />详情参见：[作业分片策略](http://dangdangdotcom.github.io/elastic-job/post/job_strategy)     |
|description                     |String |否      |     | 作业描述信息                                                                 |
|disabled                        |boolean|否      |false| 作业是否禁止启动<br />可用于部署作业时，先禁止启动，部署结束后统一启动              |
|overwrite                       |boolean|否      |false| 本地配置是否可覆盖注册中心配置<br />如果可覆盖，每次启动作业都以本地配置为准         |

#### job:listener命名空间属性详细说明

`job:listener`必须配置为`job:bean`的子元素

| 属性名                          | 类型  |是否必填|缺省值         | 描述                                                                       |
| ------------------------------ |:------|:------|:-------------|:---------------------------------------------------------------------------|
|class                           |String |`是`   |              | 前置后置任务监听实现类，需实现`ElasticJobListener`接口                                             |
|startedTimeoutMilliseconds      |long    |否    |Long.MAX_VALUE| AbstractDistributeOnceElasticJobListener型监听器，最后一个作业执行前的执行方法的超时时间<br />单位：毫秒|
|completedTimeoutMilliseconds    |long    |否    |Long.MAX_VALUE| AbstractDistributeOnceElasticJobListener型监听器，最后一个作业执行后的执行方法的超时时间<br />单位：毫秒|

#### reg:bean命名空间属性详细说明

| 属性名                          |类型   |是否必填|缺省值|描述                                                                        |
| ------------------------------ |:------|:------|:----|:---------------------------------------------------------------------------|
|id                              |String |`是`   |     | 注册中心在`Spring`容器中的主键                                                  |
|serverLists                     |String |`是`   |     | 连接`Zookeeper`服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: host1:2181,host2:2181|
|namespace                       |String |`是`   |     | `Zookeeper`的命名空间                                                         |
|baseSleepTimeMilliseconds       |int    |`是`   |     | 等待重试的间隔时间的初始值<br />单位：毫秒                                      |
|maxSleepTimeMilliseconds        |int    |`是`   |     | 等待重试的间隔时间的最大值<br />单位：毫秒                                      |
|maxRetries                      |int    |`是`   |     | 最大重试次数                                                                 |
|sessionTimeoutMilliseconds      |int    |否     |60000| 会话超时时间<br />单位：毫秒                                                  |
|connectionTimeoutMilliseconds   |int    |否     |15000| 连接超时时间<br />单位：毫秒                                                  |
|digest                          |String |否     |无验证| 连接`Zookeeper`的权限令牌<br />缺省为不需要权限验证                              |
|nestedPort                      |int    |否     |-1   | 内嵌`Zookeeper`的端口号<br />-1表示不开启内嵌`Zookeeper`                          |
|nestedDataDir                   |String |否     |     | 内嵌`Zookeeper`的数据存储路径<br />为空表示不开启内嵌`Zookeeper`                   |

### 基于Spring但不使用命名空间

```xml
    <!-- 配置作业注册中心 -->
    <bean id="regCenter" class="com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter" init-method="init">
        <constructor-arg>
            <bean class="com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration">
                <property name="serverLists" value="${xxx}" />
                <property name="namespace" value="${xxx}" />
                <property name="baseSleepTimeMilliseconds" value="${xxx}" />
                <property name="maxSleepTimeMilliseconds" value="${xxx}" />
                <property name="maxRetries" value="${xxx}" />
            </bean>
        </constructor-arg>
    </bean>
    <!-- 配置作业-->
    <bean id="xxxJob" class="com.dangdang.ddframe.job.api.JobScheduler" init-method="init">
        <constructor-arg ref="regCenter" />
        <constructor-arg>
            <bean class="com.dangdang.ddframe.job.api.JobConfiguration">
                <constructor-arg name="jobName" value="xxxJob" />
                <constructor-arg name="jobClass" value="xxxDemoJob" />
                <constructor-arg name="shardingTotalCount" value="10" />
                <constructor-arg name="cron" value="0/10 * * * * ?" />
                <property name="shardingItemParameters" value="${xxx}" />
            </bean>
        </constructor-arg>
    </bean>
```

### 不使用Spring配置

如果不使用Spring框架，可以用如下方式启动作业。

```java
import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.example.elasticjob.core.job.SimpleJobDemo;
import com.dangdang.example.elasticjob.core.job.ThroughputDataFlowJobDemo;
import com.dangdang.example.elasticjob.core.job.SequenceDataFlowJobDemo;

public class JobDemo {
    
    // 定义Zookeeper注册中心配置对象
    private ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:2181", "elastic-job-example", 1000, 3000, 3);
    
    // 定义Zookeeper注册中心
    private CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    // 定义作业1配置对象
    private JobConfiguration jobConfig1 = new JobConfiguration("simpleJob", SimpleJobDemo.class, 10, "0/5 * * * * ?");
    
    // 定义作业2配置对象
    private JobConfiguration jobConfig2 = new JobConfiguration("throughputDataFlowJob", ThroughputDataFlowJobDemo.class, 10, "0/5 * * * * ?");
    
    // 定义作业3配置对象
    private JobConfiguration jobConfig3 = new JobConfiguration("sequenceDataFlowJob", SequenceDataFlowJobDemo.class, 10, "0/5 * * * * ?");
    
    public static void main(final String[] args) {
        new JobDemo().init();
    }
    
    private void init() {
        // 连接注册中心
        regCenter.init();
        // 启动作业1
        new JobScheduler(regCenter, jobConfig1).init();
        // 启动作业2
        new JobScheduler(regCenter, jobConfig2).init();
        // 启动作业3
        new JobScheduler(regCenter, jobConfig3).init();
    }
}
```
