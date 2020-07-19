+++
title = "Operation API"
weight = 1
chapter = true
+++

## Java API(Incubating)

### 1. Configuration API

#### `JobConfigurationAPI` API for job configuration

##### YamlJobConfiguration getJobConfiguration(String jobName) Get job settings.

* **Parameters:** jobName — Job name
 
* **Returns:** Job Setting Object

##### void updateJobConfiguration(YamlJobConfiguration yamlJobConfiguration) Update job settings.

* **Parameters:** jobConfiguration — job setting object

##### void removeJobConfiguration(String jobName) Delete job settings.

* **Parameters:** jobName — Job name
 
### 2. Operation API

#### 2.1 `JobOperateAPI` API for operating jobs

##### void trigger(Optional<String> jobName, Optional<String> serverIp) The job is executed immediately. The job will only be started if it does not conflict with the last running job, and this flag will be automatically cleared after startup.

* **Parameters:**
  * jobName — Job name
  * serverIp — IP address of the job server

##### void disable(Optional<String> jobName, Optional<String> serverIp) The job disable. It will be fragmented again.

* **Parameters:**
  * jobName — Job name
  * serverIp — IP address of the job server

##### void enable(Optional<String> jobName, Optional<String> serverIp) The job enable.

* **Parameters:**
  * jobName — Job name
  * serverIp — IP address of the job server

##### void shutdown(Optional<String> jobName, Optional<String> serverIp) The job shutdown.

* **Parameters:**
  * jobName — Job name
  * serverIp — IP address of the job server

##### void remove(Optional<String> jobName, Optional<String> serverIp) The job remove.

* **Parameters:**
  * jobName — Job name
  * serverIp — IP address of the job server

#### 2.2 `ShardingOperateAPI` API for operating sharding

##### void disable(String jobName, String item) Disable the job sharding.

* **Parameters:**
  * jobName — Job name
  * item — Job sharding item

##### void enable(String jobName, String item) Enable the job sharding.

* **Parameters:**
  * jobName — Job name
  * item — Job sharding item

### 3. Statistics API

#### 3.1 `JobStatisticsAPI` API for displaying job status

##### int getJobsTotalCount() Get the total count of jobs.

* **Returns:** Total count of jobs

##### JobBriefInfo getJobBriefInfo(String jobName) Get brief job information.

* **Parameters:** jobName — Job name
 
* **Returns:** The brief job information

##### Collection<JobBriefInfo> getAllJobsBriefInfo() Get brief information about all jobs.

* **Returns:** Brief collection of all job information

##### Collection<JobBriefInfo> getJobsBriefInfo(String ip) Get brief information of all jobs under this IP.

* **Parameters:** ip — The Server IP
 
* **Returns:** Brief collection of job information

#### 3.2 `ServerStatisticsAPI` API for displaying job server status

##### int getServersTotalCount() Get the total count of job servers.

* **Returns:** Total count of job servers.

##### Collection<ServerBriefInfo> getAllServersBriefInfo() Get brief information of all job servers.

* **Returns:** Brief collection of job server information

#### 3.3 `ShardingStatisticsAPI` API for displaying job sharding status

##### Collection<ShardingInfo> getShardingInfo(String jobName) Get job sharding information collection.

* **Parameters:** jobName — Job name
 
* **Returns:** The collection of job sharding information
