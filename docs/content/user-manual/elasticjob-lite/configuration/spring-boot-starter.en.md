+++
title = "Spring Boot Starter"
weight = 2
chapter = true
+++

## Registry Center Configuration

Prefix: `elasticjob.reg-center`

Configuration: 

| Property name                   | Required |
| ------------------------------- |:-------- |
| server-lists                    | Yes      |
| namespace                       | Yes      |
| base-sleep-time-milliseconds    | No       |
| max-sleep-time-milliseconds     | No       |
| max-retries                     | No       |
| session-timeout-milliseconds    | No       |
| connection-timeout-milliseconds | No       |
| digest                          | No       |

Reference: 

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

## Job Configuration

Prefix: `elasticjob.jobs`

Configuration:

| Property name                     | Required |
| --------------------------------- |:-------- |
| elasticJobClass / elasticJobType  | Yes      |
| cron                              | No       |
| sharding-total-count              | Yes      |
| sharding-item-parameters          | No       |
| job-parameter                     | No       |
| monitor-execution                 | No       |
| failover                          | No       |
| misfire                           | No       |
| max-time-diff-seconds             | No       |
| reconcile-interval-minutes        | No       |
| job-sharding-strategy-type        | No       |
| job-executor-service-handler-type | No       |
| job-error-handler-type            | No       |
| description                       | No       |
| props                             | No       |
| disabled                          | No       |
| overwrite                         | No       |

**[elasticJobClass] are [elasticJobType] mutually exclusive.**

Reference: 

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
```

## Event Trace Configuration

Prefix: `elasticjob.tracing`

| Property name    | Options       | Required |
| -----------------|:------------- |:-------- |
| type             | RDB           | No       |

RDB is the only supported type at present.
If Spring IoC container contained a bean of DataSource and RDB was set in configuration, an instance of TracingConfiguration will be created automatically.

Reference: 

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
