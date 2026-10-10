+++
title = "Java API"
weight = 1
chapter = true
+++

## Registry Center Configuration

The component which is used to register and coordinate the distributed behavior of jobs.
ElasticJob supports `ZooKeeper` (the default), `etcd`, `Nacos` and an in-memory registry center.
Besides `elasticjob-bootstrap`, add the Maven dependency of the registry center implementation to the project:

| Registry Center | Maven Dependency                                                                    |
|-----------------|-------------------------------------------------------------------------------------|
| ZooKeeper       | `org.apache.shardingsphere.elasticjob:elasticjob-registry-center-zookeeper-curator` |
| etcd            | `org.apache.shardingsphere.elasticjob:elasticjob-registry-center-etcd`              |
| Nacos           | `org.apache.shardingsphere.elasticjob:elasticjob-registry-center-nacos`             |
| Memory          | `org.apache.shardingsphere.elasticjob:elasticjob-registry-center-memory`            |

The dependencies use `${elasticjob.version}` as their version.
Refer to [Registry Center Configuration](/en/user-manual/configuration/registry-center) for the configuration
properties and usage examples of each implementation.

The default ZooKeeper implementation is described below.

Class name: `org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration`

Configuration: 

| Name                          | Constructor injection |
|-------------------------------|:----------------------|
| serverLists                   | Yes                   |
| namespace                     | Yes                   |
| baseSleepTimeMilliseconds     | No                    |
| maxSleepTimeMilliseconds      | No                    |
| maxRetries                    | No                    |
| sessionTimeoutMilliseconds    | No                    |
| connectionTimeoutMilliseconds | No                    |
| digest                        | No                    |

## Job Configuration

Class name: `org.apache.shardingsphere.elasticjob.api.JobConfiguration`

Configuration: 

| Name                              | Constructor injection |
|-----------------------------------|:----------------------|
| jobName                           | Yes                   |
| shardingTotalCount                | Yes                   |
| cron                              | No                    |
| timeZone                          | No                    |
| shardingItemParameters            | No                    |
| jobParameter                      | No                    |
| monitorExecution                  | No                    |
| failover                          | No                    |
| misfire                           | No                    |
| maxTimeDiffSeconds                | No                    |
| reconcileIntervalMinutes          | No                    |
| jobShardingStrategyType           | No                    |
| jobExecutorThreadPoolSizeProvider | No                    |
| jobErrorHandlerType               | No                    |
| jobListenerTypes                  | No                    |
| description                       | No                    |
| props                             | No                    |
| disabled                          | No                    |
| overwrite                         | No                    |
