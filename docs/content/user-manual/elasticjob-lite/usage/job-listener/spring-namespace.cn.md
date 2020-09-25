+++
title = "使用 Spring 命名空间"
weight = 3
chapter = true
+++

## 监听器配置

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
    
    <elasticjob:job id="${myJob.id}" job-ref="myJob" registry-center-ref="regCenter" sharding-total-count="3" cron="0/1 * * * * ?" job-listener-types="simpleJobListener,distributeOnceJobListener">
    </elasticjob:job>
</beans>
```

## 作业启动

将配置 Spring 命名空间的 xml 通过 Spring 启动，作业将自动加载。
