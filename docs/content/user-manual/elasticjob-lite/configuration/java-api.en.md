+++
title = "Java API"
weight = 1
chapter = true
+++

## Registry Center Configuration

The component which is used to register and coordinate the distributed behavior of jobs, currently only supports `ZooKeeper`.

Class name: `org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration`

Configuration: 

| Name                          | Constructor injection |
| ----------------------------- |:--------------------- |
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

| Name                          | Constructor injection |
| ----------------------------- |:--------------------- |
| jobName                       | Yes                   |
| shardingTotalCount            | Yes                   |
| cron                          | No                    |
| shardingItemParameters        | No                    |
| jobParameter                  | No                    |
| monitorExecution              | No                    |
| failover                      | No                    |
| misfire                       | No                    |
| maxTimeDiffSeconds            | No                    |
| reconcileIntervalMinutes      | No                    |
| jobShardingStrategyType       | No                    |
| jobExecutorServiceHandlerType | No                    |
| jobErrorHandlerType           | No                    |
| jobListenerTypes              | No                    |
| description                   | No                    |
| props                         | No                    |
| disabled                      | No                    |
| overwrite                     | No                    |
