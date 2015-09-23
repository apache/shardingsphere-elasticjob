##Elastic-Job - distributed scheduled job solution

  Elastic-Job是ddframe中dd-job的作业模块中分离出来的分布式弹性作业框架。去掉了和dd-job中的监控和ddframe接入规范部分。该项目基于成熟的开源产品Quartz和Zookeeper及其客户端Curator进行二次开发。

  ddframe其他模块也有可独立开源的部分，之前当当曾开源过dd-soa的基石模块DubboX。
  
  elastic-job和ddframe关系见下图
  
  ![ddframe演进图](http://static.oschina.net/uploads/space/2015/0915/181703_2fxp_719192.jpg)

##主要贡献者
* 张亮 [当当网](http://www.dangdang.com/) zhangliang@dangdang.com
* 曹昊 [当当网](http://www.dangdang.com/) caohao@dangdang.com
* 江树建 [当当网](http://www.dangdang.com/) jiangshujian@dangdang.com

**讨论QQ群：**430066234（不限于Elastic-Job，包括分布式，定时任务相关以及其他互联网技术交流）

## Elastic-Job主要功能

* **定时任务：** 基于成熟的定时任务作业框架Quartz cron表达式执行定时任务。
* **作业注册中心：** 基于Zookeeper和其客户端Curator实现的全局作业注册控制中心。用于注册，控制和协调分布式作业执行。
* **作业分片：** 将一个任务分片成为多个小任务项在多服务器上同时执行。
* **弹性扩容缩容：** 运行中的作业服务器崩溃，或新增加n台作业服务器，作业框架将在下次作业执行前重新分片，不影响当前作业执行。
* **支持多种作业执行模式：** 支持OneOff，Perpetual和SequencePerpetual三种作业模式。
* **失效转移：** 运行中的作业服务器崩溃不会导致重新分片，只会在下次作业启动时分片。启用失效转移功能可以在本次作业执行过程中，监测其他作业服务器空闲，抓取未完成的孤儿分片项执行。
* **运行时状态收集：** 监控作业运行时状态，统计最近一段时间处理的数据成功和失败数量，记录作业上次运行开始时间，结束时间和下次运行时间。
* **作业停止，恢复和禁用：**用于操作作业启停，并可以禁止某作业运行（上线时常用）。
* **被错过执行的作业重触发：**自动记录错过执行的作业，并在上次作业完成后自动触发。可参考Quartz的misfire。
* **多线程快速处理数据：**使用多线程处理抓取到的数据，提升吞吐量。
* **幂等性：**重复作业任务项判定，不重复执行已运行的作业任务项。由于开启幂等性需要监听作业运行状态，对瞬时反复运行的作业对性能有较大影响。
* **容错处理：**作业服务器与Zookeeper服务器通信失败则立即停止作业运行，防止作业注册中心将失效的分片分项配给其他作业服务器，而当前作业服务器仍在执行任务，导致重复执行。
* **Spring支持：**支持spring容器，自定义命名空间，支持占位符。
* **运维平台：**提供运维界面，可以管理作业和注册中心。

## 相关文档

[目录结构说明](http://dangdangdotcom.github.io/elastic-job/folderIllustrate.html)

[使用步骤](http://dangdangdotcom.github.io/elastic-job/usage.html)

[开发指南](http://dangdangdotcom.github.io/elastic-job/userGuide.html)

[使用限制](http://dangdangdotcom.github.io/elastic-job/limited.html)

[运维平台](http://dangdangdotcom.github.io/elastic-job/webConsole.html)

[阅读源码编译问题说明](http://dangdangdotcom.github.io/elastic-job/sourceCodeGuide.html)

[实现原理](http://dangdangdotcom.github.io/elastic-job/theory.html)

[InfoQ新闻](http://www.infoq.com/cn/news/2015/09/dangdang-elastic-job)

## Quick Start

* **引入maven依赖**

elastic-job已经发布到中央仓库，可以在pom.xml文件中直接引入maven坐标。

```xml
<!-- 引入elastic-job核心模块 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-core</artifactId>
    <version>1.0.1</version>
</dependency>

<!-- 使用springframework自定义命名空间时引入 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-spring</artifactId>
    <version>1.0.1</version>
</dependency>
```
* **作业开发**

```java
public class MyElasticJob extends AbstractPerpetualElasticJob<Foo> {
    
    @Override
    protected List<Foo> fetchData(JobExecutionMultipleShardingContext context) {
        List<Foo> result = // get data from database by sharding items
        return result;
    }
    
    @Override
    protected boolean processData(JobExecutionMultipleShardingContext context, Foo data) {
        // process data
        return true;
    }
}
```

* **作业配置**

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
    
    <!-- 配置作业-->
    <job:bean id="oneOffElasticJob" class="xxx.MyElasticJob" regCenter="regCenter" cron="0/10 * * * * ?"   shardingTotalCount="3" shardingItemParameters="0=A,1=B,2=C" />
</beans>
```
