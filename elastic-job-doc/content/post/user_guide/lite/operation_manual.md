

+++
date = "2016-03-21T16:14:21+08:00"
title = "Elastic-Job-Lite操作手册"
weight=15
+++

# Elastic-Job-Lite操作手册

## Java API

### 1. 操作类API

#### a. `JobSettingsAPI` 作业配置的API.

##### `JobSettings getJobSettings(String jobName)` 获取作业设置.

* **Parameters:** `jobName` — 作业名称
 
* **Returns:** 作业设置对象.

##### `void updateJobSettings(JobSettings jobSettings)` 更新作业设置.

* **Parameters:** `jobSettings` — 作业设置对象

#### b. `JobOperateAPI` 操作作业的API.

##### `void trigger(Optional<String> jobName, Optional<String> serverIp)` 作业立刻执行.作业在不与上次运行中作业冲突的情况下才会启动, 并在启动后自动清理此标记.

* **Parameters:**
  * `jobName` — 作业名称
  * `serverIp` — 作业服务器IP地址

##### `void disable(Optional<String> jobName, Optional<String> serverIp)` 作业禁用.会重新分片.

* **Parameters:**
  * `jobName` — 作业名称
  * `serverIp` — 作业服务器IP地址

##### `void enable(Optional<String> jobName, Optional<String> serverIp)` 作业启用.

* **Parameters:**
  * `jobName` — 作业名称
  * `serverIp` — 作业服务器IP地址

##### `void shutdown(Optional<String> jobName, Optional<String> serverIp)` 作业关闭.

* **Parameters:**
  * `jobName` — 作业名称
  * `serverIp` — 作业服务器IP地址

##### `void shutdown(Optional<String> jobName, Optional<String> serverIp)` 作业删除.

* **Parameters:**
  * `jobName` — 作业名称
  * `serverIp` — 作业服务器IP地址

### 2. 统计类API

#### a. `JobStatisticsAPI` 作业状态展示的API.

##### `JobBriefInfo getJobBriefInfo(String jobName)` 获取作业简明信息.

* **Parameters:** `jobName` — 作业名称
 
* **Returns:** 作业简明信息.

##### `Collection<JobBriefInfo> getAllJobsBriefInfo()` 获取所有作业简明信息.

* **Returns:** 作业简明信息集合.

##### `Collection<JobBriefInfo> getJobsBriefInfo(String ip)` 获取该IP下所有作业简明信息.

* **Parameters:** `ip` — 服务器IP
 
* **Returns:** 作业简明信息集合.

#### b. `ServerStatisticsAPI` 作业服务器状态展示的API.

##### `Collection<ServerBriefInfo> getAllServersBriefInfo()` 获取所有作业服务器简明信息.

* **Returns:** 作业服务器简明信息集合

#### c. `ShardingStatisticsAPI` 作业分片状态展示的API.

##### `Collection<ShardingInfo> getShardingInfo(String jobName)` 获取作业分片信息集合.

* **Parameters:** `jobName` — 作业名称
 
* **Returns:** 作业分片信息集合.
