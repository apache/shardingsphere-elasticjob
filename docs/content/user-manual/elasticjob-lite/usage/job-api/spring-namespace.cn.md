+++
title = "使用 Spring 命名空间"
weight = 4
chapter = true
+++

ElasticJob-Lite 提供自定义的 Spring 命名空间，可以与 Spring 容器配合使用。
开发者能够便捷的在作业中通过依赖注入使用 Spring 容器管理的数据源等对象，并使用占位符从属性文件中取值。

## 作业配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:elasticjob="http://shardingsphere.apache.org/schema/elasticjob"
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://shardingsphere.apache.org/schema/elasticjob
                        http://shardingsphere.apache.org/schema/elasticjob/elasticjob.xsd
                        ">
    <!--配置作业注册中心 -->
    <elasticjob:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="my-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
    
    <!-- 配置作业 Bean -->
    <bean id="myJob" class="xxx.MyJob">
        <property name="fooService" ref="xxx.FooService" />
    </bean>
    
    <!-- 配置基于 class 的作业调度 -->   
    <elasticjob:job id="${myJob.id}" job-ref="myJob" registry-center-ref="regCenter" sharding-total-count="${myJob.shardingTotalCount}" cron="${myJob.cron}" />
    
    <!-- 配置基于 type 的作业调度 -->   
    <elasticjob:job id="${myScriptJob.id}" job-type="SCRIPT" registry-center-ref="regCenter" sharding-total-count="${myScriptJob.shardingTotalCount}" cron="${myScriptJob.cron}">
        <props>
            <prop key="script.command.line">${myScriptJob.scriptCommandLine}</prop>
        </props>
    </elasticjob:job>
</beans>
```

## 作业启动

将配置 Spring 命名空间的 xml 通过 Spring 启动，作业将自动加载。

## 配置作业导出端口

使用 ElasticJob-Lite 过程中可能会碰到一些分布式问题，导致作业运行不稳定。

由于无法在生产环境调试，通过 dump 命令可以把作业内部相关信息导出，方便开发者调试分析；

导出命令的使用请参见[运维指南](/cn/user-manual/elasticjob-lite/operation/dump)。

以下示例用于展示如何通过 Spring 命名空间开启用于导出命令的监听端口。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:elasticjob="http://shardingsphere.apache.org/schema/elasticjob"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/elasticjob
                           http://shardingsphere.apache.org/schema/elasticjob/elasticjob.xsd
                         ">
    <!--配置作业注册中心 -->
    <elasticjob:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="dd-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
    
    <!--配置任务快照导出服务 -->
    <elasticjob:snapshot id="jobSnapshot" registry-center-ref="regCenter" dump-port="9999" />    
</beans>
```
