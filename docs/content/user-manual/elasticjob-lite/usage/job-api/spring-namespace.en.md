+++
title = "Spring Namespace"
weight = 4
chapter = true
+++

ElasticJob-Lite provides a custom Spring namespace, which can be used with the Spring.
Through the way of DI (Dependency Injection), developers can easily use data sources and other objects that managed by the Spring container in their jobs, and use placeholders to get values ​​from property files.

## Config job for Spring namespace

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
    <bean id="myJob" class="xxx.MyJob">
        <property name="fooService" ref="xxx.FooService" />
    </bean>
    
    <!-- Configure job scheduler base on java bean -->   
    <elasticjob:job id="${myJob.id}" job-ref="myJob" registry-center-ref="regCenter" sharding-total-count="${myJob.shardingTotalCount}" cron="${myJob.cron}" />
    
   <!-- Configure job scheduler base on type --> 
    <elasticjob:job id="${myScriptJob.id}" job-type="SCRIPT" registry-center-ref="regCenter" sharding-total-count="${myScriptJob.shardingTotalCount}" cron="${myScriptJob.cron}">
        <props>
            <prop key="script.command.line">${myScriptJob.scriptCommandLine}</prop>
        </props>
    </elasticjob:job>
</beans>
```

## Start job

If the Spring container start, the xml that configures the Spring namespace will be loaded, and the job will be automatically started.
