+++
title = "使用 Spring 命名空间"
weight = 3
chapter = true
+++

## 引入 Maven 依赖

引入 elasticjob-lite-spring

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-lite-spring-namespace</artifactId>
    <version>${elasticjob.latest.version}</version>
</dependency>
```

## 配置

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
    <bean id="myJob" class="xxx.MyJob" />
    
    <!-- 配置数据源 -->
    <bean id="tracingDataSource" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="${driver.class.name}" />
        <property name="url" value="${url}" />
        <property name="username" value="${username}" />
        <property name="password" value="${password}" />
    </bean>
    <!-- 配置事件追踪 -->
    <elasticjob:rdb-tracing id="elasticJobTrace" data-source-ref="elasticJobTracingDataSource" />
    
    <!-- 配置作业 -->
    <elasticjob:job id="${myJob.id}" job-ref="myJob" registry-center-ref="regCenter" tracing-ref="elasticJobTrace" sharding-total-count="3" cron="0/1 * * * * ?" />
</beans>
```

## 作业启动

将配置 Spring 命名空间的 xml 通过 Spring 启动，作业将自动加载。
