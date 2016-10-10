
+++
date = "2016-01-27T16:14:21+08:00"
title = "Elastic-Job-Cloud部署指南"
weight=53
+++

# Elastic-Job-Cloud部署指南

## 部署步骤

1. 启动`Zookeeper`, `Mesos Master/Agent`以及`Elastic-Job-Cloud-Scheduler`。

2. 将打包之后的作业`tar.gz`文件放至网络可访问的位置，如：`ftp`或`http`。打包的`tar.gz`文件中`Main`方法需要调用`Elastic-Job-Cloud`提供的`JobBootstrap.execute`方法。

3. 使用`curl`命令调用`RESTful API`注册作业。

## RESTful API

`Elastic-Job-Cloud`提供作业注册/注销`Restful API`。可通过`curl`操作。

### 注册作业

url：`job/register`

方法：`POST`

参数类型：`application/json`

参数列表：

| 属性名                              | 类型  |是否必填 | 缺省值 | 描述                                                                              |
| -----------------------------------|:------|:-------|:------|:---------------------------------------------------------------------------------|
|jobName                             |String |`是`    |       | 作业名称。为`Elastic-Job-Cloud`的作业唯一标识                                        |
|jobClass                            |String |`是`    |       | 作业实现类                                                                         |
|jobType                             |Enum   |`是`    |       | 作业类型。`SIMPLE`，`DATAFLOW`，`SCRIPT`                                            |
|jobExecutionType                    |Enum   |`是`    |       | 作业执行类型。`TRANSIENT`为瞬时作业，`DAEMON`为常驻作业                                |
|cron                                |String |`是`    |       | `cron`表达式，用于配置作业触发时间                                                    |
|shardingTotalCount                  |int    |`是`    |       | 作业分片总数                                                                       |
|cpuCount                            |double |`是`    |       | 单片作业所需要的`CPU`数量，最小值为`0.01`                                             |
|memoryMB                            |double |`是`    |       | 单片作业所需要的内存`MB`，最小值为`32`                                                |
|appURL                              |String |`是`    |       | 应用所在路径。必须是可以通过网络访问到的路径                                            |
|bootstrapScript                     |String |`是`    |       | 启动脚本，如：`bin\start.sh`。                                                      |
|failover                            |boolean|否      |`false`| 是否开启失效转移                                                                    |
|misfire                             |boolean|否      |`false`| 是否开启错过任务重新执行                                                             |
|beanName                            |String |否      |       | `Spring`容器中配置的`bean`名称                                                      |
|applicationContext                  |String |否      |       | `Spring`方式配置`Spring`配置文件相对路径以及名称，如：`META-INF\applicationContext.xml`|
|jobEventConfigs                     |String |否      |       | 作业事件配置，目前可配置`log`和`rdb`监听器，如:`{"log":{},"rdb":{"driverClassName":"com.mysql.jdbc.Driver", "url":"jdbc:mysql://your_host:3306/elastic_job_log", "username":"root", "password":"", "logLevel":"WARN"}}`                                     |
|scriptCommandLine                   |String |否      |       | `SCRIPT`类型作业命令行执行脚本                                                      |

注册的作业可用`Java`和`Spring`两种启动方式，作业启动在[开发指南](../dev_guide/)中有说明，这里只举例说明两种方式如何注册。

**Java启动方式作业注册**

```shell
curl -l -H "Content-type: application/json" -X POST -d 
'{"jobName":"foo_job","jobClass":"yourJobClass","jobType":"SIMPLE","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appURL":"http://app_host:8080/foo-job.tar.gz","failover":true,"misfire":true,"bootstrapScript":"bin/start.sh"}' 
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
