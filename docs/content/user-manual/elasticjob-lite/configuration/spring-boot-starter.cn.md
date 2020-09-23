+++
title = "Spring Boot Starter"
weight = 2
chapter = true
+++

## 注册中心配置

配置前缀：`elasticjob.reg-center`

可配置属性：

| 属性名                          | 是否必填 |
| ------------------------------- |:-------- |
| server-lists                    | 是       |
| namespace                       | 是       |
| base-sleep-time-milliseconds    | 否       |
| max-sleep-time-milliseconds     | 否       |
| max-retries                     | 否       |
| session-timeout-milliseconds    | 否       |
| connection-timeout-milliseconds | 否       |
| digest                          | 否       |

配置格式参考：

**YAML**
```yaml
elasticjob:
  regCenter:
    serverLists: localhost:6181
    namespace: elasticjob-lite-springboot
```

**Properties**
```
elasticjob.reg-center.namespace=elasticjob-lite-springboot
elasticjob.reg-center.server-lists=localhost:6181
```

## 作业配置

配置前缀：`elasticjob.jobs`

可配置属性：

| 属性名                            | 是否必填 |
| --------------------------------- |:-------- |
| elasticJobClass / elasticJobType  | 是       |
| cron                              | 否       |
| jobBootstrapBeanName              | 否       |
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
| job-listener-types                | 否       |
| description                       | 否       |
| props                             | 否       |
| disabled                          | 否       |
| overwrite                         | 否       |

**elasticJobClass 与 elasticJobType 互斥，每项作业只能有一种类型**

如果配置了 cron 属性则为定时调度作业，Starter 会在应用启动时自动启动；
否则为一次性调度作业，需要通过 jobBootstrapBeanName 指定 OneOffJobBootstrap Bean 的名称，
在触发点注入 OneOffJobBootstrap 的实例并手动调用 execute() 方法。

配置格式参考：

**YAML**
```yaml
elasticjob:
  jobs:
    simpleJob:
      elasticJobClass: org.apache.shardingsphere.elasticjob.lite.example.job.SpringBootSimpleJob
      cron: 0/5 * * * * ?
      shardingTotalCount: 3
      shardingItemParameters: 0=Beijing,1=Shanghai,2=Guangzhou
    scriptJob:
      elasticJobType: SCRIPT
      cron: 0/10 * * * * ?
      shardingTotalCount: 3
      props:
        script.command.line: "echo SCRIPT Job: "
    manualScriptJob:
      elasticJobType: SCRIPT
      jobBootstrapBeanName: manualScriptJobBean
      shardingTotalCount: 9
      props:
        script.command.line: "echo Manual SCRIPT Job: "
```

**Properties**
```
elasticjob.jobs.simpleJob.elastic-job-class=org.apache.shardingsphere.elasticjob.lite.example.job.SpringBootSimpleJob
elasticjob.jobs.simpleJob.cron=0/5 * * * * ?
elasticjob.jobs.simpleJob.sharding-total-count=3
elasticjob.jobs.simpleJob.sharding-item-parameters=0=Beijing,1=Shanghai,2=Guangzhou
elasticjob.jobs.scriptJob.elastic-job-type=SCRIPT
elasticjob.jobs.scriptJob.cron=0/5 * * * * ?
elasticjob.jobs.scriptJob.sharding-total-count=3
elasticjob.jobs.scriptJob.props.script.command.line=echo SCRIPT Job:
elasticjob.jobs.manualScriptJob.elastic-job-type=SCRIPT
elasticjob.jobs.manualScriptJob.job-bootstrap-bean-name=manualScriptJobBean
elasticjob.jobs.manualScriptJob.sharding-total-count=3
elasticjob.jobs.manualScriptJob.props.script.command.line=echo Manual SCRIPT Job:
```

## 事件追踪配置

配置前缀：`elasticjob.tracing`

| 属性名           | 可选值        | 是否必填 |
| -----------------|:------------- |:-------- |
| type             | RDB           | 否       |

目前仅提供了 RDB 类型的事件追踪数据源实现。
Spring IoC 容器中存在 DataSource 类型的 bean 且配置数据源类型为 RDB 时会自动配置事件追踪，无须显式创建。

配置格式参考：

**YAML**
```yaml
elasticjob:
  tracing:
    type: RDB
```

**Properties**
```
elasticjob.tracing.type=RDB
```

## 作业信息导出配置

配置前缀：`elasticjob.dump`

| 属性名           | 缺省值        | 是否必填 |
| -----------------|:------------- |:-------- |
| enabled          | true          | 否       |
| port             |               | 是       |

Spring Boot 提供了作业信息导出端口快速配置，只需在配置中指定导出所用的端口号即可启用导出功能。
如果没有指定端口号，导出功能不会生效。

配置参考：

**YAML**
```yaml
elasticjob:
  dump:
    port: 9888
```

**Properties**
```
elasticjob.dump.port=9888
```
