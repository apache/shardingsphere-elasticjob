+++
title = "Use Spring Namespace"
weight = 4
chapter = true
+++

ElasticJob-Lite provides a custom Spring namespace, which can be used with the Spring.
Through the way of DI (Dependency Injection), developers can easily use data sources and other objects that managed by the Spring container in their jobs, and use placeholders to get values ​​from property files.

## Job Configuration

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

## Job Start

If the Spring container start, the `XML` that configures the Spring namespace will be loaded, and the job will be automatically started.

## Job Dump

Using ElasticJob may meet some distributed problem which is not easy to observe.

Because of developer can not debug in production environment, ElasticJob provide `dump` command to export job runtime information for debugging.

Please refer to [Operation Manual](/en/user-manual/elasticjob-lite/operation/dump) for more details.

The example below is how to configure SnapshotService for open listener port to dump.

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
    <!--Create registry center -->
    <elasticjob:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="dd-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
    
    <!--Configure the task snapshot export service -->
    <elasticjob:snapshot id="jobSnapshot" registry-center-ref="regCenter" dump-port="9999" />    
</beans>
```

## Configuration error handler strategy

In the process of using ElasticJob-Lite, when the job is abnormal, the following error handling strategies can be used.

| *Error handler strategy name*            | *Description*                                                 |  *Built-in*  | *Default*| *Extra config*   |
| ---------------------------------------- | ------------------------------------------------------------- |  -------     |  --------|  --------------  |
| Log Strategy                             | Log error and do not interrupt job                            |   Yes        |     Yes  |                  |
| Throw Strategy                           | Throw system exception and interrupt job                      |   Yes        |          |                  |
| Ignore Strategy                          | Ignore exception and do not interrupt job                     |   Yes        |          |                  |
| Email Notification Strategy              | Send email message notification and do not interrupt job      |              |          |    Yes           |
| Wechat Enterprise Notification Strategy  | Send wechat message notification and do not interrupt job     |              |          |    Yes           |
| Dingtalk Notification Strategy           | Send dingtalk message notification and do not interrupt job   |              |          |    Yes           |

The following example shows how to configure the error-handling policy through the Spring namespace.


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
    
    <!-- Log Strategy -->
    <elasticjob:job  ... job-error-handler-type="LOG"   />

    <!-- Throw Strategy -->
    <elasticjob:job  ... job-error-handler-type="THROW"   />

    <!-- Ignore Strategy -->
    <elasticjob:job  ... job-error-handler-type="IGNORE"   />

    <!-- Email Notification Strategy  -->
    <elasticjob:email-error-handler id="emailErrorHandlerConfig" host="host" port="465" username="username"
                                    password="password" use-ssl="true" subject="ElasticJob error message"
                                    from="from@xxx.com" to="to1@xxx.com,to2@xxx.com"
                                    cc="cc@xxx.com" bcc="bcc@xxx.com"
                                    debug="false"/>

    <elasticjob:job  ... job-error-handler-type="EMAIL"  error-handler-config-ref="emailErrorHandlerConfig" />


    <!-- Wechat Enterprise Notification Strategy -->
    <elasticjob:wechat-error-handler id="wechatErrorHandlerConfig"
                                        webhook="you_webhook"
                                        connect-timeout-millisecond="3000"
                                        read-timeout-millisecond="5000"/>

    <elasticjob:job  ... job-error-handler-type="WECHAT"  error-handler-config-ref="wechatErrorHandlerConfig" />

    <!-- Dingtalk Notification Strategy  -->
    <elasticjob:dingtalk-error-handler id="dingtalkErrorHandlerConfig"
                                       webhook="you_webhook"
                                       keyword="keyword" secret="secret"
                                       connect-timeout-millisecond="3000"
                                       read-timeout-millisecond="5000"/>

    <elasticjob:job  ... job-error-handler-type="DINGTALK"  error-handler-config-ref="dingtalkErrorHandlerConfig" />
</beans>
```