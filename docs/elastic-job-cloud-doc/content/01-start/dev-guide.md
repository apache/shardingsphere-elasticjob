+++
toc = true
date = "2016-01-27T16:14:21+08:00"
title = "开发指南"
weight = 3
prev = "/01-start/faq/"
next = "/01-start/deploy-guide/"
+++

## 0. 环境需求

### Java

请使用JDK1.7及其以上版本。[详情参见](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

### Zookeeper

请使用Zookeeper 3.4.6及其以上版本。[详情参见](https://zookeeper.apache.org/doc/trunk/zookeeperStarted.html)

### Maven

请使用Maven 3.0.4及其以上版本。[详情参见](http://maven.apache.org/install.html)

### Mesos

请使用Mesos 0.28.0及其以上版本。[详情参见](http://mesos.apache.org/gettingstarted/)

## 1. 作业开发

Elastic-Job-Lite和Elastic-Job-Cloud提供统一作业接口，开发者仅需对业务作业进行一次开发，之后可根据不同的配置以及部署至不同的Lite或Cloud环境。

Elastic-Job提供Simple、Dataflow和Script 3种作业类型。
方法参数shardingContext包含作业配置、片和运行时信息。可通过getShardingTotalCount(), getShardingItem()等方法分别获取分片总数，运行在本作业服务器的分片序列号等。

### a. Simple类型作业

意为简单实现，未经任何封装的类型。需实现SimpleJob接口。该接口仅提供单一方法用于覆盖，此方法将定时执行。与Quartz原生接口相似，但提供了弹性扩缩容和分片等功能。

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

Dataflow类型用于处理数据流，需实现DataflowJob接口。该接口提供2个方法可供覆盖，分别用于抓取(fetchData)和处理(processData)数据。

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

可通过DataflowJobConfiguration配置是否流式处理。

流式处理数据只有fetchData方法的返回值为null或集合长度为空时，作业才停止抓取，否则作业将一直运行下去；
非流式处理数据则只会在每次作业执行过程中执行一次fetchData方法和processData方法，随即完成本次作业。

如果采用流式作业处理方式，建议processData处理数据后更新其状态，避免fetchData再次抓取到，从而使得作业永不停止。
流式数据处理参照TbSchedule设计，适用于不间歇的数据处理。

### c. Script类型作业

Script类型作业意为脚本类型作业，支持shell，python，perl等所有类型脚本。只需通过控制台或代码配置scriptCommandLine即可，无需编码。执行脚本路径可包含参数，参数传递完毕后，作业框架会自动追加最后一个参数为作业运行时信息。

```
#!/bin/bash
echo sharding execution context is $*
```

作业运行时输出

sharding execution context is {"jobName":"scriptElasticDemoJob","shardingTotalCount":10,"jobParameter":"","shardingItem":0,"shardingParameter":"A"}

## 3. 作业启动

### a. Java启动方式

需定义Main方法并调用JobBootstrap.execute()，例子如下：

```java
public class JobDemo {
    
    public static void main(final String[] args) {
        JobBootstrap.execute();
    }
}
```

### b. Spring启动方式

同Java启动方式，但需要通过REST API配置bean的名字和Spring配置文件位置，如：

```json
{..., "beanName":"simpleJobBean", "applicationContext":"yourDir/applicationContext.xml"}
```

之后将作业和用于执行Java Main方法的Shell脚本打包为gz.tar格式，然后使用Cloud提供的REST API将其部署至Elastic-Job-Cloud系统。如对如何打包不理解请参考我们提供的example。
