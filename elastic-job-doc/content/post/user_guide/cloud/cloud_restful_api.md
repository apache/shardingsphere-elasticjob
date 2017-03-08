+++
date = "2017-02-17T14:14:21+08:00"
title = "Elastic-Job-Cloud REST API"
weight=56
+++

# Elastic-Job-Cloud RESTful API

`Elastic-Job-Cloud`提供APP发布及作业注册等`RESTful API`可通过`curl`操作。

### 发布作业APP

url：`app`

方法：`POST`

参数类型：`application/json`

参数列表：

| 属性名                              | 类型  |是否必填 | 缺省值 | 描述                                                                              |
| -----------------------------------|:------|:-------|:------|:---------------------------------------------------------------------------------|
|appName                             |String |`是`    |       | 作业应用名称。为`Elastic-Job-Cloud`的作业应用唯一标识                                 |
|appURL                              |String |`是`    |       | 作业应用所在路径。必须是可以通过网络访问到的路径                                        |
|cpuCount                            |double |否      |   1   | 作业应用启动所需要的`CPU`数量                                                        |
|memoryMB                            |double |否      |  128  | 作业应用启动所需要的内存`MB`                                                         |
|bootstrapScript                     |String |`是`    |       | 启动脚本，如：`bin\start.sh`                                                        |
|appCacheEnable                      |bool   |否      | true  | 每次执行作业时是否从缓存中读取应用。禁用则每次执行任务均从应用仓库下载应用至本地             |
|eventTraceSamplingCount             |int    |否      | 0     | 常驻作业事件采样率统计条数，默认不采样全部记录。为避免数据量过大，可对频繁调度的常驻作业配置采样率，即作业每执行N次，才会记录作业执行及追踪相关数据|

```shell
curl -l -H "Content-type: application/json" -X POST -d 
'{"appName":"foo_app","appURL":"http://app_host:8080/foo-job.tar.gz","cpuCount":0.1,"memoryMB":64.0,"bootstrapScript":"bin/start.sh","appCacheEnable":true,"eventTraceSamplingCount":0}' 
http://elastic_job_cloud_host:8899/app
```

### 修改作业APP配置

url：`app`

方法：`PUT`

参数类型：`application/json`

参数列表：

| 属性名                              | 类型  |是否必填 | 缺省值 | 描述                                                                              |
| -----------------------------------|:------|:-------|:------|:---------------------------------------------------------------------------------|
|appName                             |String |`是`    |       | 作业应用名称。为`Elastic-Job-Cloud`的作业应用唯一标识                                 |
|appCacheEnable                      |bool   |`是`    | true  | 每次执行作业时是否从缓存中读取应用。禁用则每次执行任务均从应用仓库下载应用至本地             |
|eventTraceSamplingCount             |int    |否      | 0     | 常驻作业事件采样率统计条数，默认不采样全部记录。为避免数据量过大，可对频繁调度的常驻作业配置采样率，即作业每执行N次，才会记录作业执行及追踪相关数据|

```shell
curl -l -H "Content-type: application/json" -X PUT -d 
'{"appName":"foo_app","appCacheEnable":true}' 
http://elastic_job_cloud_host:8899/app
```

### 注册作业

url：`job/register`

方法：`POST`

参数类型：`application/json`

参数列表：

| 属性名                              | 类型  |是否必填 | 缺省值 | 描述                                                                              |
| -----------------------------------|:------|:-------|:------|:---------------------------------------------------------------------------------|
|jobName                             |String |`是`    |       | 作业名称。为`Elastic-Job-Cloud`的作业唯一标识                                        |
|appName                             |String |`是`    |       | 作业应用名称。                                                                      |
|jobClass                            |String |`是`    |       | 作业实现类                                                                         |
|jobType                             |Enum   |`是`    |       | 作业类型。`SIMPLE`，`DATAFLOW`，`SCRIPT`                                            |
|jobExecutionType                    |Enum   |`是`    |       | 作业执行类型。`TRANSIENT`为瞬时作业，`DAEMON`为常驻作业                                |
|cron                                |String |`是`    |       | `cron`表达式，用于配置作业触发时间                                                    |
|shardingTotalCount                  |int    |`是`    |       | 作业分片总数                                                                       |
|cpuCount                            |double |`是`    |       | 单片作业所需要的`CPU`数量，最小值为`0.001`                                            |
|memoryMB                            |double |`是`    |       | 单片作业所需要的内存`MB`，最小值为`1`                                                 |
|appURL                              |String |`是`    |       | 应用所在路径。必须是可以通过网络访问到的路径                                            |
|bootstrapScript                     |String |`是`    |       | 启动脚本，如：`bin\start.sh`。                                                      |
|shardingItemParameters              |String |否      |       | 分片序列号和参数用等号分隔，多个键值对用逗号分隔<br />分片序列号从`0`开始，不可大于或等于作业分片总数<br />如：<br/>`0=a,1=b,2=c`|
|jobParameter                        |String |否      |       | 作业自定义参数<br />作业自定义参数，可通过传递该参数为作业调度的业务方法传参，用于实现带参数的作业<br />例：每次获取的数据量、作业实例从数据库读取的主键等 |
|failover                            |boolean|否      |`false`| 是否开启失效转移                                                                    |
|misfire                             |boolean|否      |`false`| 是否开启错过任务重新执行                                                             |
|beanName                            |String |否      |       | `Spring`容器中配置的`bean`名称                                                      |
|applicationContext                  |String |否      |       | `Spring`方式配置`Spring`配置文件相对路径以及名称，如：`META-INF\applicationContext.xml`|
|streamingProcess                    |boolean|否      |`false`| `DATAFLOW`类型作业，是否流式处理数据<br />如果流式处理数据, 则`fetchData`不返回空结果将持续执行作业<br />如果非流式处理数据, 则处理数据完成后作业结束<br />|
|scriptCommandLine                   |String |否      |       | `SCRIPT`类型作业命令行执行脚本                                                      |
|jobProperties                       |String |否      |       | 作业定制化属性，目前支持`job_exception_handler`和`executor_service_handler`，用于扩展异常处理和自定义作业处理线程池 |
|description                         |String |否      |       | 作业描述信息                                                                       |

注册的作业可用`Java`和`Spring`两种启动方式，作业启动在[开发指南](../dev_guide/)中有说明，这里只举例说明两种方式如何注册。

使用`Transient`的`Script`类型作业可直接将`shell`上传至`appURL`，而无需打成`java`包。如果只有单个`shell`文件可无需压缩。如是复杂`shell`或`python`等应用，仍可上传`tar`包，支持各种常见压缩格式。

**Java启动方式作业注册**

```shell
curl -l -H "Content-type: application/json" -X POST -d 
'{"jobName":"foo_job","appName":"foo_app","jobClass":"yourJobClass","jobType":"SIMPLE","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appURL":"http://app_host:8080/foo-job.tar.gz","failover":true,"misfire":true,"bootstrapScript":"bin/start.sh"}' 
http://elastic_job_cloud_host:8899/job/register
```

**Spring启动方式作业注册**

```shell
curl -l -H "Content-type: application/json" -X POST -d 
'{"jobName":"foo_job","jobClass":"yourJobClass","beanName":"yourBeanName","applicationContext":"applicationContext.xml","jobType":"SIMPLE","jobExecutionType":"TRANSIENT",
"cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appURL":"http://file_host:8080/foo-job.tar.gz","failover":false,"misfire":true,"bootstrapScript":"bin/start.sh"}' 
http://elastic_job_cloud_masterhost:8899/job/register
```

### 修改作业配置

url：`job/update`

方法：`PUT`

参数类型：`application/json`

参数：同注册作业

```shell
curl -l -H "Content-type: application/json" -X PUT -d 
'{"jobName":"foo_job","jobClass":"yourJobClass","jobType":"SIMPLE","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appURL":"http://app_host:8080/foo-job.tar.gz","failover":true,"misfire":true,"bootstrapScript":"bin/start.sh"}' 
http://elastic_job_cloud_host:8899/job/update
```

### 注销作业

url：`job/deregister`

方法：`DELETE`

参数类型：`application/json`

参数：作业名称

```shell
curl -l -H "Content-type: application/json" -X DELETE -d 'foo_job' http://elastic_job_cloud_host:8899/job/deregister
```

### 触发一次作业

url：`job/trigger`

方法：`POST`

参数类型：`application/json`

参数：作业名称

说明：即事件驱动，通过调用`API`而非定时的触发作业。目前仅对`Transient`作业类型生效。

```shell
curl -l -H "Content-type: application/json" -X POST -d 'foo_job' http://elastic_job_cloud_host:8899/job/trigger
```
