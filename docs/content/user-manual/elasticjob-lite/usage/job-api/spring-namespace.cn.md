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

### 定时调度

将配置 Spring 命名空间的 xml 通过 Spring 启动，作业将自动加载。

### 一次性调度

一次性调度的作业的执行权在开发者手中，开发者可以在需要调用作业的位置注入 `OneOffJobBootstrap`，
通过 `execute()` 方法执行作业。

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


## 配置错误处理策略

使用 ElasticJob-Lite 过程中当作业发生异常后，可采用以下错误处理策略。

| *错误处理策略名称*         | *说明*                            |  *是否内置* | *是否默认*| *是否需要额外配置* |
| ----------------------- | --------------------------------- |  -------  |  --------|  -------------  |
| 记录日志策略              | 记录作业异常日志，但不中断作业执行     |   是       |     是   |                 |
| 抛出异常策略              | 抛出系统异常并中断作业执行            |   是       |         |                 |
| 忽略异常策略              | 忽略系统异常且不中断作业执行          |   是       |          |                 |
| 邮件通知策略              | 发送邮件消息通知，但不中断作业执行     |            |          |      是         |
| 企业微信通知策略           | 发送企业微信消息通知，但不中断作业执行 |            |          |      是          |
| 钉钉通知策略              | 发送钉钉消息通知，但不中断作业执行     |            |          |      是          |

以下示例用于展示如何通过 Spring 命名空间配置错误处理策略。


```xml
<?xml version="1.0" encoding="UTF-8"?>
<elasticjob:job xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:elasticjob="http://shardingsphere.apache.org/schema/elasticjob"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/elasticjob
                           http://shardingsphere.apache.org/schema/elasticjob/elasticjob.xsd
                         ">
    <!-- 记录日志策略 -->
    <elasticjob:job  ... job-error-handler-type="LOG" />
    
    <!-- 抛出异常策略 -->
    <elasticjob:job  ... job-error-handler-type="THROW" />
    
    <!-- 忽略异常策略 -->
    <elasticjob:job  ... job-error-handler-type="IGNORE" />
    
    <!-- 邮件通知策略 -->    
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
    
    <!-- 企业微信通知策略 -->
    <elasticjob:job  ... job-error-handler-type="WECHAT">
        <props>
            <prop key="wechat.webhook">${webhook}</prop>
            <prop key="wechat.connectTimeoutMilliseconds">${connectTimeoutMilliseconds}</prop>
            <prop key="wechat.readTimeoutMilliseconds">${readTimeoutMilliseconds}</prop>
        </props>
    </elasticjob:job>
    
    <!-- 钉钉通知策略 -->
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
