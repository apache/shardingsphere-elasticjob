+++
title = "Use Spring Namespace"
weight = 3
chapter = true
+++

## Listener configuration

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
    <!-- Configuration job registration center -->
    <elasticjob:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="my-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
    
    <!-- Configuration Job Bean -->
    <bean id="myJob" class="xxx.MyJob" />
    
    <elasticjob:job id="${myJob.id}" job-ref="myJob" registry-center-ref="regCenter" sharding-total-count="3" cron="0/1 * * * * ?" job-listener-types="simpleJobListener,distributeOnceJobListener">
    </elasticjob:job>
</beans>
```

## Job start

The xml that configures the Spring namespace is started through Spring, and the job will be automatically loaded.

