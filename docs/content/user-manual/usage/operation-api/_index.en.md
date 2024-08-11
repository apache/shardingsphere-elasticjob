+++
title = "Operation API"
weight = 4
chapter = true
+++

ElasticJob provides a Java API, which can control the life cycle of jobs in a distributed environment by directly operating the registry.

The module is still in incubation. Possible dependency configurations are as follows,

```xml
<dependency>
  <groupId>org.apache.shardingsphere.elasticjob</groupId>
  <artifactId>elasticjob-lifecycle</artifactId>
  <version>${elasticjob.version}</version>
</dependency>
```

## Configuration API

Class name: `org.apache.shardingsphere.elasticjob.lifecycle.api.JobConfigurationAPI`

### Get job configuration

Method signature：YamlJobConfiguration getJobConfiguration(String jobName)

* **Parameters:** 
  * jobName — Job name

* **Returns:** Job configuration object

### Update job configuration

Method signature：void updateJobConfiguration(YamlJobConfiguration yamlJobConfiguration)

* **Parameters:** 
  * jobConfiguration — Job configuration object

### Remove job configuration 

Method signature：void removeJobConfiguration(String jobName)

* **Parameters:** 
  * jobName — Job name

## Operation API

Class name：`org.apache.shardingsphere.elasticjob.lifecycle.api.JobOperateAPI`

### Trigger job execution

The job will only trigger execution if it does not conflict with the currently running job, and this flag will be automatically cleared after it is started.

Method signature：void trigger(Optional<String> jobName)

* **Parameters:**
  * jobName — Job name

### Disable job

Disabling a job will cause other distributed jobs to trigger resharding.

Method signature：void disable(Optional<String> jobName, Optional<String> serverIp)

* **Parameters:**
  * jobName — Job name
  * serverIp — job server IP address

### Enable job

Method signature：void enable(Optional<String> jobName, Optional<String> serverIp)

* **Parameters:**
  * jobName — Job name
  * serverIp — job server IP address

### Shutdown scheduling job

Method signature：void shutdown(Optional<String> jobName, Optional<String> serverIp)

* **Parameters:**
  * jobName — Job name
  * serverIp — IP address of the job server

### Remove job

Method signature：void remove(Optional<String> jobName, Optional<String> serverIp)

* **Parameters:**
  * jobName — Job name
  * serverIp — IP address of the job server

### Dump job

Method signature：String dump(String jobName, String instanceIp, int dumpPort)

* **Parameters:**
  * jobName — Job name
  * serverIp — IP address of the job server
  * dumpPort — Dump port

## Operate sharding API

Class name：`org.apache.shardingsphere.elasticjob.lifecycle.api.ShardingOperateAPI`

### Disable job sharding

Method signature：void disable(String jobName, String item)

* **Parameters:**
  * jobName — Job name
  * item — Job sharding item

### Enable job sharding

Method signature：void enable(String jobName, String item)

* **Parameters:**
  * jobName — Job name
  * item — Job sharding item

## Job statistics API

Class name：`org.apache.shardingsphere.elasticjob.lifecycle.api.JobStatisticsAPI`

### Get the total count of jobs

Method signature：int getJobsTotalCount()

* **Returns:** the total count of jobs

### Get brief job information

Method signature：JobBriefInfo getJobBriefInfo(String jobName)

* **Parameters:**
  * jobName — Job name
 
* **Returns:** The brief job information

### Get brief information about all jobs.

Method signature：Collection<JobBriefInfo> getAllJobsBriefInfo()

* **Returns:** Brief collection of all job information

### Get brief information of all jobs under this IP

Method signature：Collection<JobBriefInfo> getJobsBriefInfo(String ip)

* **Parameters:**
  * ip — server IP
 
* **Returns:** Brief collection of job information

## Job server status display API

Class name：`org.apache.shardingsphere.elasticjob.lifecycle.api.ServerStatisticsAPI`

### Total count of job servers

Method signature：int getServersTotalCount()

* **Returns:** Get the total count of job servers

### Get brief information about all job servers

Method signature：Collection<ServerBriefInfo> getAllServersBriefInfo()

* **Returns:** Brief collection of job information

## Job sharding status display API

Class name：`org.apache.shardingsphere.elasticjob.lifecycle.api.ShardingStatisticsAPI`

### Get job sharding information collection

Method signature：Collection<ShardingInfo> getShardingInfo(String jobName)

* **Parameters:**
  * jobName — Job name
 
* **Returns:** The collection of job sharding information
