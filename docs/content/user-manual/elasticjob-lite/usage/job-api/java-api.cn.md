+++
title = "使用 Java API"
weight = 2
chapter = true
+++

## 作业配置

ElasticJob-Lite 采用构建器模式创建作业配置对象。
代码示例如下：

```java
    JobConfiguration jobConfig = JobConfiguration.newBuilder("myJob", 3).cron("0/5 * * * * ?").shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").build();
```

## 作业启动

ElasticJob-Lite 调度器分为定时调度和一次性调度两种类型。
每种调度器启动时均需要注册中心配置、作业对象（或作业类型）以及作业配置这 3 个参数。

### 定时调度

```java
public class JobDemo {
    
    public static void main(String[] args) {
        // 调度基于 class 类型的作业
        new ScheduleJobBootstrap(createRegistryCenter(), new MyJob(), createJobConfiguration()).schedule();
        // 调度基于 type 类型的作业
        new ScheduleJobBootstrap(createRegistryCenter(), "MY_TYPE", createJobConfiguration()).schedule();
    }
    
    private static CoordinatorRegistryCenter createRegistryCenter() {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration("zk_host:2181", "elastic-job-demo"));
        regCenter.init();
        return regCenter;
    }
    
    private static JobConfiguration createJobConfiguration() {
        // 创建作业配置
        ...
    }
}
```

### 一次性调度

```java
public class JobDemo {
    
    public static void main(String[] args) {
        OneOffJobBootstrap jobBootstrap = new OneOffJobBootstrap(createRegistryCenter(), new MyJob(), createJobConfiguration());
        // 可多次调用一次性调度
        jobBootstrap.execute();
        jobBootstrap.execute();
        jobBootstrap.execute();
    }
    
    private static CoordinatorRegistryCenter createRegistryCenter() {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration("zk_host:2181", "elastic-job-demo"));
        regCenter.init();
        return regCenter;
    }
    
    private static JobConfiguration createJobConfiguration() {
        // 创建作业配置
        ...
    }
}
```

## 配置作业导出端口

使用 ElasticJob-Lite 过程中可能会碰到一些分布式问题，导致作业运行不稳定。

由于无法在生产环境调试，通过 dump 命令可以把作业内部相关信息导出，方便开发者调试分析；

导出命令的使用请参见[运维指南](/cn/user-manual/elasticjob-lite/operation/dump)。

以下示例用于展示如何通过 SnapshotService 开启用于导出命令的监听端口。

```java
public class JobMain {
    
    public static void main(final String[] args) {
        SnapshotService snapshotService = new SnapshotService(regCenter, 9888).listen();
    }
    
    private static CoordinatorRegistryCenter createRegistryCenter() {
        // 创建注册中心
    }
}
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

### 记录日志策略
```java
public class JobDemo {
    
    public static void main(String[] args) {
        //  定时调度作业
        new ScheduleJobBootstrap(createRegistryCenter(), new MyJob(), createScheduleJobConfiguration()).schedule();
        // 一次性调度作业
        new OneOffJobBootstrap(createRegistryCenter(), new MyJob(), createOneOffJobConfiguration()).execute();
    }
    
    private static JobConfiguration createScheduleJobConfiguration() {
        // 创建定时作业配置， 并且使用记录日志策略
        return JobConfiguration.newBuilder("myScheduleJob", 3).cron("0/5 * * * * ?").jobErrorHandlerType("LOG").build();
    }

    private static JobConfiguration createOneOffJobConfiguration() {
        // 创建一次性作业配置， 并且使用记录日志策略
        return JobConfiguration.newBuilder("myOneOffJob", 3).jobErrorHandlerType("LOG").build();
    }

    private static CoordinatorRegistryCenter createRegistryCenter() {
        // 配置注册中心
        ...
    }
}
```

### 抛出异常策略
```java
public class JobDemo {
    
    public static void main(String[] args) {
        //  定时调度作业
        new ScheduleJobBootstrap(createRegistryCenter(), new MyJob(), createScheduleJobConfiguration()).schedule();
        // 一次性调度作业
        new OneOffJobBootstrap(createRegistryCenter(), new MyJob(), createOneOffJobConfiguration()).execute();
    }
    
    private static JobConfiguration createScheduleJobConfiguration() {
        // 创建定时作业配置， 并且使用抛出异常策略
        return JobConfiguration.newBuilder("myScheduleJob", 3).cron("0/5 * * * * ?").jobErrorHandlerType("THROW").build();
    }

    private static JobConfiguration createOneOffJobConfiguration() {
        // 创建一次性作业配置， 并且使用抛出异常策略
        return JobConfiguration.newBuilder("myOneOffJob", 3).jobErrorHandlerType("THROW").build();
    }

    private static CoordinatorRegistryCenter createRegistryCenter() {
        // 配置注册中心
        ...
    }
}
```


### 忽略异常策略
```java
public class JobDemo {
    
    public static void main(String[] args) {
        //  定时调度作业
        new ScheduleJobBootstrap(createRegistryCenter(), new MyJob(), createScheduleJobConfiguration()).schedule();
        // 一次性调度作业
        new OneOffJobBootstrap(createRegistryCenter(), new MyJob(), createOneOffJobConfiguration()).execute();
    }
    
    private static JobConfiguration createScheduleJobConfiguration() {
        // 创建定时作业配置， 并且使用忽略异常策略
        return JobConfiguration.newBuilder("myScheduleJob", 3).cron("0/5 * * * * ?").jobErrorHandlerType("IGNORE").build();
    }

    private static JobConfiguration createOneOffJobConfiguration() {
        // 创建一次性作业配置， 并且使用忽略异常策略
        return JobConfiguration.newBuilder("myOneOffJob", 3).jobErrorHandlerType("IGNORE").build();
    }

    private static CoordinatorRegistryCenter createRegistryCenter() {
        // 配置注册中心
        ...
    }
}
```

### 邮件通知策略

请参考 [这里](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler/#邮件通知策略) 了解更多。

Maven POM:
```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-email</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```
```java
public class JobDemo {
    
    public static void main(String[] args) {
        //  定时调度作业
        new ScheduleJobBootstrap(createRegistryCenter(), new MyJob(), createScheduleJobConfiguration()).schedule();
        // 一次性调度作业
        new OneOffJobBootstrap(createRegistryCenter(), new MyJob(), createOneOffJobConfiguration()).execute();
    }
    
    private static JobConfiguration createScheduleJobConfiguration() {
        // 创建定时作业配置， 并且使用邮件通知策略
        JobConfiguration jobConfig = JobConfiguration.newBuilder("myScheduleJob", 3).cron("0/5 * * * * ?").jobErrorHandlerType("EMAIL").build();
        setEmailProperties(jobConfig);
        return jobConfig;

    }

    private static JobConfiguration createOneOffJobConfiguration() {
        // 创建一次性作业配置， 并且使用邮件通知策略
        JobConfiguration jobConfig = JobConfiguration.newBuilder("myOneOffJob", 3).jobErrorHandlerType("EMAIL").build();
        setEmailProperties(jobConfig);
        return jobConfig;
    }

    private static void setEmailProperties(final JobConfiguration jobConfig) {
        // 设置邮件的配置
        jobConfig.getProps().setProperty(EmailPropertiesConstants.HOST, "host");
        jobConfig.getProps().setProperty(EmailPropertiesConstants.PORT, "465");
        jobConfig.getProps().setProperty(EmailPropertiesConstants.USERNAME, "username");
        jobConfig.getProps().setProperty(EmailPropertiesConstants.PASSWORD, "password");
        jobConfig.getProps().setProperty(EmailPropertiesConstants.FROM, "from@xxx.xx");
        jobConfig.getProps().setProperty(EmailPropertiesConstants.TO, "to1@xxx.xx,to1@xxx.xx");
    }

    private static CoordinatorRegistryCenter createRegistryCenter() {
        // 配置注册中心
        ...
    }
}
```

### 企业微信通知策略

请参考 [这里](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler/#企业微信通知策略) 了解更多。

Maven POM:
```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-wechat</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```
```java
public class JobDemo {
    
    public static void main(String[] args) {
        //  定时调度作业
        new ScheduleJobBootstrap(createRegistryCenter(), new MyJob(), createScheduleJobConfiguration()).schedule();
        // 一次性调度作业
        new OneOffJobBootstrap(createRegistryCenter(), new MyJob(), createOneOffJobConfiguration()).execute();
    }
    
    private static JobConfiguration createScheduleJobConfiguration() {
        // 创建定时作业配置， 并且使用企业微信通知策略
        JobConfiguration jobConfig = JobConfiguration.newBuilder("myScheduleJob", 3).cron("0/5 * * * * ?").jobErrorHandlerType("WECHAT").build();
        setWechatProperties(jobConfig);
        return jobConfig;

    }

    private static JobConfiguration createOneOffJobConfiguration() {
        // 创建一次性作业配置， 并且使用企业微信通知策略
        JobConfiguration jobConfig = JobConfiguration.newBuilder("myOneOffJob", 3).jobErrorHandlerType("WECHAT").build();
        setWechatProperties(jobConfig);
        return jobConfig;
    }

    private static void setWechatProperties(final JobConfiguration jobConfig) {
        // 设置企业微信的配置
        jobConfig.getProps().setProperty(WechatPropertiesConstants.WEBHOOK, "you_webhook");
    }

    private static CoordinatorRegistryCenter createRegistryCenter() {
        // 配置注册中心
        ...
    }
}
```


### 钉钉通知策略

请参考 [这里](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler/#钉钉通知策略) 了解更多。

Maven POM:
```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-dingtalk</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```
```java
public class JobDemo {
    
    public static void main(String[] args) {
        //  定时调度作业
        new ScheduleJobBootstrap(createRegistryCenter(), new MyJob(), createScheduleJobConfiguration()).schedule();
        // 一次性调度作业
        new OneOffJobBootstrap(createRegistryCenter(), new MyJob(), createOneOffJobConfiguration()).execute();
    }
    
    private static JobConfiguration createScheduleJobConfiguration() {
        // 创建定时作业配置， 并且使用企业微信通知策略
        JobConfiguration jobConfig = JobConfiguration.newBuilder("myScheduleJob", 3).cron("0/5 * * * * ?").jobErrorHandlerType("DINGTALK").build();
        setDingtalkProperties(jobConfig);
        return jobConfig;

    }

    private static JobConfiguration createOneOffJobConfiguration() {
        // 创建一次性作业配置， 并且使用钉钉通知策略
        JobConfiguration jobConfig = JobConfiguration.newBuilder("myOneOffJob", 3).jobErrorHandlerType("DINGTALK").build();
        setDingtalkProperties(jobConfig);
        return jobConfig;
    }

    private static void setDingtalkProperties(final JobConfiguration jobConfig) {
        // 设置钉钉的配置
        jobConfig.getProps().setProperty(DingtalkPropertiesConstants.WEBHOOK, "you_webhook");
        jobConfig.getProps().setProperty(DingtalkPropertiesConstants.KEYWORD, "you_keyword");
        jobConfig.getProps().setProperty(DingtalkPropertiesConstants.SECRET, "you_secret");
    }

    private static CoordinatorRegistryCenter createRegistryCenter() {
        // 配置注册中心
        ...
    }
}
```