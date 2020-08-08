+++
pre = "<b>4.1.2 </b>"
title = "Configuration"
weight = 2
chapter = true
+++

Through which developers can quickly and clearly understand the functions provided by ElasticJob-Lite.

This chapter is a configuration manual for ElasticJob-Lite, which can also be referred to as a dictionary if necessary.

ElasticJob-Lite has provided 3 kinds of configuration methods for different situations.

## Registry Center Configuration

### Configuration

| Name                          | Data Type     | Default Value | Description                                              |
| ----------------------------- |:------------- |:------------- |:-------------------------------------------------------- |
| serverLists                   | String        |               | ZooKeeper server IP list                                 |
| namespace                     | String        |               | ZooKeeper namespace                                      |
| baseSleepTimeMilliseconds     | int           | 1000          | The initial value of milliseconds for the retry interval |
| maxSleepTimeMilliseconds      | String        | 3000          | The maximum value of milliseconds for the retry interval |
| maxRetries                    | String        | 3             | Maximum number of retries                                |
| sessionTimeoutMilliseconds    | boolean       | 60000         | Session timeout in milliseconds                          |
| connectionTimeoutMilliseconds | boolean       | 15000         | Connection timeout in milliseconds                       |
| digest                        | String        | no need       | Permission token to connect to ZooKeeper                 |

### Core Configuration Description

**serverLists:**

Include IP and port, multiple addresses are separated by commas, such as: `host1:2181,host2:2181`

## Job Configuration

### Configuration

| Name                          | Data Type       | Default Value        | Description                                                                           |
| ----------------------------- |:--------------- |:-------------------- |:------------------------------------------------------------------------------------  |
| jobName                       | String          |                      | Job name                                                                              |
| shardingTotalCount            | int             |                      | Sharding total count                                                                  |
| cron                          | String          |                      | CRON expression, control the job trigger time                                         |
| shardingItemParameters        | String          |                      | Sharding item parameters                                                              |
| jobParameter                  | String          |                      | Job parameter                                                                         |
| monitorExecution              | boolean         | true                 | Monitor job execution status                                                          |
| failover                      | boolean         | false                | Enable or disable job failover                                                        |
| misfire                       | boolean         | true                 | Enable or disable the missed task to re-execute                                       |
| maxTimeDiffSeconds            | int             | -1(no check)         | The maximum value for time difference between server and registry center in seconds   |
| reconcileIntervalMinutes      | int             | 10                   | Service scheduling interval in minutes for repairing job server inconsistent state    |
| jobShardingStrategyType       | String          | AVG_ALLOCATION       | Job sharding strategy type                                                            |
| jobExecutorServiceHandlerType | String          | CPU                  | Job thread pool handler type                                                          |
| jobErrorHandlerType           | String          |                      | Job error handler type                                                                |
| description                   | String          |                      | Job description                                                                       |
| props                         | Properties      |                      | Job properties                                                                        |
| disabled                      | boolean         | false                | Enable or disable start the job                                                       |
| overwrite                     | boolean         | false                | Enable or disable local configuration override registry center configuration          |

### Core Configuration Description

**shardingItemParameters:**

The sequence numbers and parameters of the Sharding items are separated by equal sign, and multiple key-value pairs are separated by commas.
The Sharding sequence number starts from `0` and can't be greater than or equal to the total number of job fragments.
For example: `0=a,1=b,2=c`

**jobParameter:**

With this parameter, user can pass parameters for the business method of job scheduling, which is used to implement the job with parameters.
For example: `Amount of data acquired each time`, `Primary key of the job instance read from the database`, etc.

**monitorExecution:**

When the execution time and interval of each job are very short, it is recommended not to monitor the running status of the job to improve efficiency.
There is no need to monitor because it is a transient state. User can add data accumulation monitoring by self. And there is no guarantee that the data will be selected repeatedly, idempotency should be achieved in the job.
If the job execution time and interval time are longer, it is recommended to monitor the job status, and it can guarantee that the data will not be selected repeatedly.

**failover:**

Enable failover and monitorExecution together to take effect.

**maxTimeDiffSeconds:**

If the time error exceeds the configured seconds, an exception will be thrown when the job starts.

**reconcileIntervalMinutes:**

In a distributed system, due to network, clock and other reasons, ZooKeeper may be inconsistent with the actual running job. This inconsistency cannot be completely avoided through positive verification.
It is necessary to start another thread to periodically calibrate the consistency between the registry center and the job status, that is, to maintain the final consistency of ElasticJob.

Less than `1` means no repair is performed.

**jobShardingStrategyType:**

For details, see[Job Sharding Strategy](/en/user-manual/elasticjob-lite/configuration/built-in-strategy/sharding)。

**jobExecutorServiceHandlerType:**

For details, see[Thread Pool Strategy](/en/user-manual/elasticjob-lite/configuration/built-in-strategy/thread-pool)。

**jobErrorHandlerType:**

For details, see[Error Handler Strategy](/en/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler)。

**props:**

For details, see[Job Properties](/en/user-manual/elasticjob-lite/configuration/props)。

**disabled:**

It can be used for deployment, forbid jobs to start, and then start them uniformly after the deployment is completed.

**overwrite:**

If the value is `true`, local configuration override registry center configuration every time the job is started.

## Job Listener Configuration

### Common Listener Configuration

Configuration: no

### Distributed Listener Configuration

Configuration

| Name                           | Data Type    | Default Value  | Description                                                 |
| ------------------------------ |:------------ |:-------------- |:----------------------------------------------------------- |
| started-timeout-milliseconds   | long         | Long.MAX_VALUE | The timeout in milliseconds before the last job is executed |
| completed-timeout-milliseconds | long         | Long.MAX_VALUE | The timeout in milliseconds after the last job is executed  |

## Event Tracing Configuration

### Configuration

| Name    | Data Type      | Default Value | Description                                 |
| ------- |:-------------- |:------------- |:------------------------------------------- |
| type    | String         |               | The type of event tracing storage adapter   |
| storage | Generics Type  |               | The object of event tracing storage adapter |
