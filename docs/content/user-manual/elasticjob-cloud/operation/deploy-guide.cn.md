+++
title = "部署指南"
weight = 1
chapter = true
+++

## Scheduler部署步骤

1. 启动Elastic-Job-Cloud-Scheduler和Mesos指定注册中心的Zookeeper。

2. 启动Mesos Master和Mesos Agent。

3. 解压elastic-job-cloud-scheduler-${version}.tar.gz。可通过源码mvn install编译获取。

4. 执行bin\start.sh脚本启动elastic-job-cloud-scheduler。

## 作业部署步骤

1. 确保Zookeeper, Mesos Master/Agent以及Elastic-Job-Cloud-Scheduler已启动。

2. 将打包之后的作业tar.gz文件放至网络可访问的位置，如：ftp或http。打包的tar.gz文件中Main方法需要调用Elastic-Job-Cloud提供的JobBootstrap.execute方法。

3. 使用curl命令调用REST API注册APP及作业，详情参见：[RESTful API](/02-guide/cloud-restful-api)。

# 附录

* 配置：修改conf\elastic-job-cloud-scheduler.properties文件。配置项说明如下：

| 属性名称                          | 必填     | 默认值                      | 描述                                                                                        |
| -------------------------------- |:--------|:----------------------------|:-------------------------------------------------------------------------------------------|
| hostname                         | 是    |                             | 服务器真实的IP或hostname，不能是127.0.0.1或localhost                                   |
| user                             | 否      |                             | Mesos framework使用的用户名称                                                              |
| mesos_url                        | 是    | zk://127.0.0.1:2181/mesos   | Mesos所使用的Zookeeper地址                                                               |
| zk_servers                       | 是    | 127.0.0.1:2181              | Elastic-Job-Cloud所使用的Zookeeper地址                                                   |
| zk_namespace                     | 否      | elastic-job-cloud           | Elastic-Job-Cloud所使用的Zookeeper命名空间                                                |
| zk_digest                        | 否      |                             | Elastic-Job-Cloud所使用的Zookeeper登录凭证                                                |
| http_port                        | 是    | 8899                        | Restful API所使用的端口号                                                                   |
| job_state_queue_size             | 是    | 10000                       | 堆积作业最大值, 超过此阀值的堆积作业将直接丢弃。阀值过大可能会导致Zookeeper无响应，应根据实测情况调整  |
| event_trace_rdb_driver           | 否      |                             | 作业事件追踪数据库驱动                                                                         |
| event_trace_rdb_url              | 否      |                             | 作业事件追踪数据库URL                                                                         |
| event_trace_rdb_username         | 否      |                             | 作业事件追踪数据库用户名                                                                       |
| event_trace_rdb_password         | 否      |                             | 作业事件追踪数据库密码                                                                         |

***

* 停止：不提供停止脚本，可直接使用kill杀进程。
