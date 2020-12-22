+++
pre = "<b>4.2.2. </b>"
title = "配置手册"
weight = 2
chapter = true
+++

ElasticJob-Cloud 提供应用发布及作业注册等 RESTful API， 可通过 curl 操作。

请求 url 前缀为 `/api`

## 鉴权 API

### 获取 AccessToken

url: login

方法：POST

参数类型：application/json

参数列表：

| 属性名                   | 类型    | 是否必填 | 缺省值     | 描述                           |
| ----------------------- |:------- |:------- |:--------- |:----------------------------- |
| username                | String  | 是      |           | API 鉴权用户名                 |
| password                | String  | 是      |           | API 鉴权密码                 |

响应体：

| 属性名                   | 类型      | 描述                           |
| ----------------------- |:-------  |:----------------------------- |
| accessToken             | String   | API 鉴权 token              |

示例：

```bash
curl -H "Content-Type: application/json" -X POST http://elasticjob_cloud_host:8899/api/login -d '{"username": "root", "password": "pwd"}'
```

响应体：

```json
{"accessToken":"some_token"}
```

## 应用 API

### 发布应用

url：app

方法：POST

参数类型：application/json

参数列表：

| 属性名                   | 类型    | 是否必填 | 缺省值     | 描述                           |
| ----------------------- |:------- |:------- |:--------- |:----------------------------- |
| appName                 | String  | 是      |           | 作业应用名称                    |
| appURL                  | String  | 是      |           | 作业应用所在路径                 |
| cpuCount                | double  | 否      | 1         | 作业应用启动所需要的 CPU 数量     |
| memoryMB                | double  | 否      | 128       | 作业应用启动所需要的内存 MB       |
| bootstrapScript         | String  | 是      |           | 启动脚本                        |
| appCacheEnable          | boolean | 否      | true      | 每次执行作业时是否从缓存中读取应用 |
| eventTraceSamplingCount | int     | 否      | 0（不采样）| 常驻作业事件采样率统计条数         |

参数详细说明：

**appName:**

为 ElasticJob-Cloud 的作业应用唯一标识。

**appURL:**

必须提供可以通过网络访问的路径。

**bootstrapScript:**

如：bin\start.sh

**appCacheEnable:**

禁用则每次执行任务均从应用仓库下载应用至本地。

**eventTraceSamplingCount:**

为避免数据量过大，可对频繁调度的常驻作业配置采样率，即作业每执行 N 次，才会记录作业执行及追踪相关数据。

示例：

```bash
curl -l -H "Content-type: application/json" -X POST -d '{"appName":"my_app","appURL":"http://app_host:8080/my-job.tar.gz","cpuCount":0.1,"memoryMB":64.0,"bootstrapScript":"bin/start.sh","appCacheEnable":true,"eventTraceSamplingCount":0}' http://elastic_job_cloud_host:8899/api/app
```

### 修改应用配置

url：app

方法：PUT

参数类型：application/json

参数列表：

| 属性名                   | 类型    | 是否必填 | 缺省值     | 描述                             |
| ----------------------- |:------- |:------- |:--------- |:------------------------------- |
| appName                 | String  | 是      |           | 作业应用名称                     |
| appCacheEnable          | boolean | 是      | true      | 每次执行作业时是否从缓存中读取应用  |
| eventTraceSamplingCount | int     | 否      | 0（不采样）| 常驻作业事件采样率统计条数         |

示例：

```bash
curl -l -H "Content-type: application/json" -X PUT -d '{"appName":"my_app","appCacheEnable":true}' http://elastic_job_cloud_host:8899/api/app
```

## 作业 API

### 注册作业

url：job/register

方法：POST

参数类型：application/json

参数列表：

| 属性名                         | 类型       | 是否必填 | 缺省值  | 描述                                               |
| ----------------------------- |:---------- |:------- |:------ |:------------------------------------------------- |
| appName                       | String     | 是      |        | 作业应用名称                                        |
| cpuCount                      | double     | 是      |        | 单片作业所需要的 CPU 数量，最小值为 0.001             |
| memoryMB                      | double     | 是      |        | 单片作业所需要的内存 MB，最小值为 1                   |
| jobExecutionType              | Enum       | 是      |        | 作业执行类型。TRANSIENT 为瞬时作业，DAEMON 为常驻作业 |
| jobName                       | String     | 是      |        | 作业名称                                          |
| cron                          | String     | 否      |        | cron 表达式，用于配置作业触发时间                    |
| shardingTotalCount            | int        | 是      |        | 作业分片总数                                       |
| shardingItemParameters        | String     | 否      |        | 自定义分片参数                                     |
| jobParameter                  | String     | 否      |        | 作业自定义参数                                     |
| failover                      | boolean    | 否      | false  | 是否开启失效转移                                    |
| misfire                       | boolean    | 否      | false  | 是否开启错过任务重新执行                             |
| jobExecutorServiceHandlerType | boolean    | 否      | false  | 作业线程池处理策略                                  |
| jobErrorHandlerType           | boolean    | 否      | false  | 作业错误处理策略                                    |
| description                   | String     | 否      |        | 作业描述信息                                        |
| props                         | Properties | 否      |        | 作业属性配置信息                                     |

使用脚本类型的瞬时作业可直接将脚本上传至 appURL，而无需打成 tar 包。
如果只有单个脚本文件可无需压缩。
如是复杂脚本应用，仍可上传 tar 包，支持各种常见压缩格式。

示例：

```bash
curl -l -H "Content-type: application/json" -X POST -d '{"appName":"my_app","cpuCount":0.1,"memoryMB":64.0,"jobExecutionType":"TRANSIENT","jobName":"my_job","cron":"0/5 * * * * ?","shardingTotalCount":5,"failover":true,"misfire":true}' http://elastic_job_cloud_host:8899/api/job/register
```

### 修改作业配置

url：job/update

方法：PUT

参数类型：application/json

参数：同注册作业

示例：

```bash
curl -l -H "Content-type: application/json" -X PUT -d '{"appName":"my_app","jobName":"my_job","cpuCount":0.1,"memoryMB":64.0,"jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"failover":true,"misfire":true}' http://elastic_job_cloud_host:8899/api/job/update
```

### 注销作业

url：job/deregister

方法：DELETE

参数类型：application/json

参数：作业名称

示例：

```bash
curl -l -H "Content-type: application/json" -X DELETE -d 'my_job' http://elastic_job_cloud_host:8899/api/job/deregister
```

### 触发一次作业

url：job/trigger

方法：POST

参数类型：application/json

参数：作业名称

说明：即事件驱动，通过调用 API 而非定时的触发作业。目前仅对瞬时作业生效。

示例：

```bash
curl -l -H "Content-type: application/json" -X POST -d 'my_job' http://elastic_job_cloud_host:8899/api/job/trigger
```
