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

### Schedule Job

If the Spring container start, the `XML` that configures the Spring namespace will be loaded, and the job will be automatically started.

### One-off Job

When to execute OneOffJob is up to you. 
Developers can inject the `OneOffJobBootstrap` bean into where they plan to invoke.
Trigger the job by invoking `execute()` method manually.

```xml
    <bean id="oneOffJob" class="org.apache.shardingsphere.elasticjob.lite.example.job.simple.SpringSimpleJob" />
    <elasticjob:job id="oneOffJobBean" job-ref="oneOffJob" ...  />
```
```java
public final class SpringMain {
    public static void main(final String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:META-INF/application-context.xml");
        OneOffJobBootstrap oneOffJobBootstrap = context.getBean("oneOffJobBean", OneOffJobBootstrap.class);
        oneOffJobBootstrap.execute();
    }
}
```

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
    <elasticjob:job  ... job-error-handler-type="LOG" />
    
    <!-- Throw Strategy -->
    <elasticjob:job  ... job-error-handler-type="THROW" />
    
    <!-- Ignore Strategy -->
    <elasticjob:job  ... job-error-handler-type="IGNORE" />
    
    <!-- Email Notification Strategy  -->
    <elasticjob:job ... job-error-handler-type="EMAIL">
        <props>
            <prop key="email.host">${host}</prop>
            <prop key="email.port">${port}</prop>
            <prop key="email.username">${username}</prop>
            <prop key="email.password">${password}</prop>
            <prop key="email.useSsl">${useSsl}</prop>
            <prop key="email.subject">${subject}</prop>
            <prop key="email.from">${from}</prop>
            <prop key="email.to">${to}</prop>
            <prop key="email.cc">${cc}</prop>
            <prop key="email.bcc">${bcc}</prop>
            <prop key="email.debug">${debug}</prop>
        </props>
    </elasticjob:job>
    
    <!-- Wechat Enterprise Notification Strategy -->
    <elasticjob:job  ... job-error-handler-type="WECHAT">
        <props>
            <prop key="wechat.webhook">${webhook}</prop>
            <prop key="wechat.connectTimeoutMilliseconds">${connectTimeoutMilliseconds}</prop>
            <prop key="wechat.readTimeoutMilliseconds">${readTimeoutMilliseconds}</prop>
        </props>
    </elasticjob:job>
    
    <!-- Dingtalk Notification Strategy  -->
    <elasticjob:job  ... job-error-handler-type="DINGTALK">
        <props>
            <prop key="dingtalk.webhook">${webhook}</prop>
            <prop key="dingtalk.keyword">${keyword}</prop>
            <prop key="dingtalk.secret">${secret}</prop>
            <prop key="dingtalk.connectTimeoutMilliseconds">${connectTimeoutMilliseconds}</prop>
            <prop key="dingtalk.readTimeoutMilliseconds">${readTimeoutMilliseconds}</prop>
        </props>
    </elasticjob:job>
</beans>
```
