+++
pre = "<b>2. </b>"
title = "快速入门"
weight = 2
chapter = true
+++

## 1. 使用 API 配置启动

### 引入 maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-lite-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### 作业开发

```java
public class MyElasticJob implements SimpleJob {
    
    @Override
    public void execute(ShardingContext context) {
        switch (context.getShardingItem()) {
            case 0: 
                // do something by sharding item 0
                break;
            case 1: 
                // do something by sharding item 1
                break;
            case 2: 
                // do something by sharding item 2
                break;
            // case n: ...
        }
    }
}
```

### 作业配置

```java
    // 定义作业核心配置
    JobCoreConfiguration simpleCoreConfig = JobCoreConfiguration.newBuilder("demoSimpleJob", "0/15 * * * * ?", 10).build();
    // 定义 SIMPLE 类型配置
    SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(simpleCoreConfig, SimpleDemoJob.class.getCanonicalName());
    // 定义作业根配置
    JobConfiguration jobConfig = JobConfiguration.newBuilder(simpleJobConfig).build();
```

### 启动作业

```java
public class JobDemo {
    
    public static void main(String[] args) {
        new JobScheduler(createRegistryCenter(), createJobConfiguration()).init();
    }
    
    private static CoordinatorRegistryCenter createRegistryCenter() {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration("zk_host:2181", "elastic-job-demo"));
        regCenter.init();
        return regCenter;
    }
    
    private static JobConfiguration createJobConfiguration() {
        // 创建作业配置
        // ...
    }
}
```

## 2. 使用 Spring 配置启动

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-lite-spring</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### 作业开发

同使用 API 配置中作业开发

### 作业配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:elasticjob="http://elasticjob.shardingsphere.apache.org/schema/elasticjob"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://elasticjob.shardingsphere.apache.org/schema/elasticjob
                           http://elasticjob.shardingsphere.apache.org/schema/elasticjob/elasticjob.xsd
                           ">
    <!--配置作业注册中心 -->
    <elasticjob:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="elastic-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
   
    <!--配置任务监控 -->
    <elasticjob:monitor id="monitor1" registry-center-ref="regCenter" monitor-port="9999" />    

    <!--配置作业类 -->
    <bean id="simpleJob" class="xxx.MyElasticJob" />
    
    <!--配置作业 -->
    <elasticjob:simple id="oneOffElasticJob" job-ref="simpleJob" registry-center-ref="regCenter" reconcile-interval-minutes="10" cron="0/10 * * * * ?" sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
</beans>
```

### 启动作业

将配置 Spring 命名空间的 xml 通过 Spring 启动，作业将自动加载。
