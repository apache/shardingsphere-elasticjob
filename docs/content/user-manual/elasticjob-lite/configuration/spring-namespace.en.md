+++
title = "Spring Namespace"
weight = 2
chapter = true
+++

To use the Spring namespace, user need to add the dependency of the `elasticjob-lite-spring` module in the `pom.xml` file.

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-lite-spring-namespace</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

Spring namespace: [http://shardingsphere.apache.org/schema/elasticjob/elasticjob.xsd](http://shardingsphere.apache.org/schema/elasticjob/elasticjob.xsd)

## Registry Center Configuration

\<elasticjob:zookeeper />

Configuration: 

| Name                            | Required |
| ------------------------------- |:-------- |
| id                              | Yes      |
| server-lists                    | Yes      |
| namespace                       | Yes      |
| base-sleep-time-milliseconds    | No       |
| max-sleep-time-milliseconds     | No       |
| max-retries                     | No       |
| session-timeout-milliseconds    | No       |
| connection-timeout-milliseconds | No       |
| digest                          | No       |

## Job Configuration

\<elasticjob:job />

Configuration: 

| Name                              | Required |
| --------------------------------- |:-------- |
| id                                | Yes      |
| class                             | No       |
| job-ref                           | No       |
| registry-center-ref               | Yes      |
| tracing-ref                       | No       |
| cron                              | Yes      |
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

## Event Tracing Configuration

\<elasticjob:rdb-tracing />

Configuration:

| Name            | Data Type  | Required | Default Value | Description                                     |
| --------------- |:---------- |:-------- |:------------- |:-------------------------------------------     |
| id              | String     | Yes      |               | The bean's identify of the event tracing        |
| data-source-ref | DataSource | No       |               | The bean's name of the event tracing DataSource |

## Job Dump Configuration

\<elasticjob:snapshot />

Configuration: 

| Name                | Data Type   | Required | Default Value | Description                                                                     |
| ------------------- |:----------- |:-------- |:------------- |:------------------------------------------------------------------------------- |
| id                  | String      | Yes      |               | The identify of the monitoring service in the Spring container                  |
| registry-center-ref | String      | Yes      |               | Registry center bean's reference, need to the statement of the `reg:zookeeper`  |
| dump-port           | String      | Yes      |               | Job dump port<br />usage: echo "dump@jobName" \| nc 127.0.0.1 9888             |
