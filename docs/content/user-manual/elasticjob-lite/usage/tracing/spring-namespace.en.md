+++
title = "Use Spring Namespace"
weight = 3
chapter = true
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-lite-spring-namespace</artifactId>
    <version>${elasticjob.latest.version}</version>
</dependency>
```

## Configuration

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
    <!-- Configure registry center for job -->
    <elasticjob:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="my-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
    
    <!-- Configure job java bean -->
    <bean id="myJob" class="xxx.MyJob" />
    
    <!-- Configure DataSource -->
    <bean id="tracingDataSource" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="${driver.class.name}" />
        <property name="url" value="${url}" />
        <property name="username" value="${username}" />
        <property name="password" value="${password}" />
    </bean>
    <!-- Configure event tracing -->
    <elasticjob:rdb-tracing id="elasticJobTrace" data-source-ref="elasticJobTracingDataSource" />
    
    <!-- Configure job -->
    <elasticjob:job id="${myJob.id}" job-ref="myJob" registry-center-ref="regCenter" tracing-ref="elasticJobTrace" sharding-total-count="3" cron="0/1 * * * * ?" />
</beans>
```

## Job Start

If the Spring container start, the `XML` that configures the Spring namespace will be loaded, and the job will be automatically started.

