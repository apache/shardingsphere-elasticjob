+++
title = "Spring 命名空间"
weight = 3
chapter = true
+++

使用 Spring 命名空间需在 pom.xml 文件中添加 elasticjob-lite-spring 模块的依赖。

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-lite-spring-namespace</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

命名空间：[http://shardingsphere.apache.org/schema/elasticjob/elasticjob.xsd](http://shardingsphere.apache.org/schema/elasticjob/elasticjob.xsd)

## 注册中心配置

\<elasticjob:zookeeper />

可配置属性：

| 属性名                           | 是否必填 |
| ------------------------------- |:------- |
| id                              | 是      |
| server-lists                    | 是      |
| namespace                       | 是      |
| base-sleep-time-milliseconds    | 否      |
| max-sleep-time-milliseconds     | 否      |
| max-retries                     | 否      |
| session-timeout-milliseconds    | 否      |
| connection-timeout-milliseconds | 否      |
| digest                          | 否      |

## 作业配置

\<elasticjob:job />

可配置属性：

| 属性名                             | 是否必填  |
| --------------------------------- |:-------- |
| id                                | 是       |
| class                             | 否       |
| job-ref                           | 否       |
| registry-center-ref               | 是       |
| tracing-ref                       | 否       |
| cron                              | 是       |
| sharding-total-count              | 是       |
| sharding-item-parameters          | 否       |
| job-parameter                     | 否       |
| monitor-execution                 | 否       |
| failover                          | 否       |
| misfire                           | 否       |
| max-time-diff-seconds             | 否       |
| reconcile-interval-minutes        | 否       |
| job-sharding-strategy-type        | 否       |
| job-executor-service-handler-type | 否       |
| job-error-handler-type            | 否       |
| description                       | 否       |
| props                             | 否       |
| disabled                          | 否       |
| overwrite                         | 否       |

## 作业监听配置

\<elasticjob:listener />

elasticjob:listener 必须配置为 elasticjob:job 的子元素，并且在子元素中只允许出现一次

可配置属性：

| 属性名  | 类型   | 是否必填 | 缺省值 | 描述                                               |
| ------ |:------ |:------- |:----- |:-------------------------------------------------- |
| class  | String | 是      |       | 前置后置任务监听实现类，需实现 ElasticJobListener 接口 |

\<elasticjob:distributed-listener />

elasticjob:distributed-listener 必须配置为 elasticjob:job 的子元素，并且在子元素中只允许出现一次

可配置属性：

| 属性名                          | 类型   | 是否必填 | 缺省值          | 描述                                                                        |
| ------------------------------ |:------ |:------- |:-------------- |:--------------------------------------------------------------------------- |
| class                          | String | 是      |                | 前置后置任务分布式监听实现类，需继承 AbstractDistributeOnceElasticJobListener 类 |
| started-timeout-milliseconds   | long   | 否      | Long.MAX_VALUE | 最后一个作业执行前的执行方法的超时毫秒数                                         |
| completed-timeout-milliseconds | long   | 否      | Long.MAX_VALUE | 最后一个作业执行后的执行方法的超时毫秒数                                         |

## 事件追踪配置

\<elasticjob:rdb-event-trace />

可配置属性：

| 属性名           | 类型       | 是否必填 | 缺省值 | 描述                  |
| --------------- |:---------- |:------- |:----- |:--------------------- |
| id              | String     | 是      |       | 事件追踪 Bean 主键      |
| data-source-ref | DataSource | 是      |       | 事件追踪数据源 Bean 名称 |

## 快照导出配置

\<elasticjob:snapshot />

可配置属性：

| 属性名               | 类型   | 是否必填 | 缺省值 | 描述                                                                     |
| ------------------- |:------ |:------ |:------ |:------------------------------------------------------------------------ |
| id                  | String | 是     |        | 监控服务在 Spring 容器中的主键                                              |
| registry-center-ref | String | 是     |        | 注册中心 Bean 的引用，需引用 reg:zookeeper 的声明                            |
| dump-port           | String | 是     |        | 导出作业信息数据端口<br />使用方法: echo "dump@jobName" \| nc 127.0.0.1 9888 |
