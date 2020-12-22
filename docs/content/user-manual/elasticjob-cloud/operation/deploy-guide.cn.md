+++
title = "部署指南"
weight = 1
chapter = true
+++

## 调度器部署步骤

1. 启动 ElasticJob-Cloud-Scheduler 和 Mesos 指定作为注册中心的 ZooKeeper
1. 启动 Mesos Master 和 Mesos Agent
1. 解压 `elasticjob-cloud-scheduler-${version}.tar.gz`
1. 执行 `bin\start.sh` 脚本启动 elasticjob-cloud-scheduler

## 作业部署步骤

1. 确保 ZooKeeper, Mesos Master/Agent 以及 ElasticJob-Cloud-Scheduler 已正确启动
1. 将打包作业的 tar.gz 文件放至网络可访问的位置，如：ftp或http。打包的 tar.gz 文件中 `main` 方法需要调用 ElasticJob-Cloud 提供的 `JobBootstrap.execute` 方法
1. 使用 curl 命令调用 RESTful API 发布应用及注册作业。详情请参见：[配置指南](/cn/user-manual/elasticjob-cloud/configuration)

## 调度器配置步骤

可修改 `conf\elasticjob-cloud-scheduler.properties` 文件变更系统配置。

配置项说明：

| 属性名称                  | 是否必填 | 默认值                     | 描述                                                                                       |
| ------------------------ |:------- |:------------------------- |:------------------------------------------------------------------------------------------ |
| hostname                 | 是      |                           | 服务器真实的 IP 或 hostname，不能是 127.0.0.1 或 localhost                                    |
| user                     | 否      |                           | Mesos framework 使用的用户名称                                                               |
| mesos_url                | 是      | zk://127.0.0.1:2181/mesos | Mesos 所使用的 ZooKeeper 地址                                                                |
| zk_servers               | 是      | 127.0.0.1:2181            | ElasticJob-Cloud 所使用的 ZooKeeper 地址                                                     |
| zk_namespace             | 否      | elasticjob-cloud          | ElasticJob-Cloud 所使用的 ZooKeeper 命名空间                                                  |
| zk_digest                | 否      |                           | ElasticJob-Cloud 所使用的 ZooKeeper 登录凭证                                                  |
| http_port                | 是      | 8899                      | RESTful API 所使用的端口号                                                                    |
| job_state_queue_size     | 是      | 10000                     | 堆积作业最大值, 超过此阀值的堆积作业将直接丢弃。阀值过大可能会导致 ZooKeeper 无响应，应根据实测情况调整 |
| event_trace_rdb_driver   | 否      |                           | 作业事件追踪数据库驱动                                                                         |
| event_trace_rdb_url      | 否      |                           | 作业事件追踪数据库 URL                                                                         |
| event_trace_rdb_username | 否      |                           | 作业事件追踪数据库用户名                                                                       |
| event_trace_rdb_password | 否      |                           | 作业事件追踪数据库密码                                                                         |
| auth_username            | 否      | root                      | API 鉴权用户名                                                                               |
| auth_password            | 否      | pwd                       | API 鉴权密码                                                                                 |

***

* 停止：不提供停止脚本，可直接使用 kill 命令终止进程。
