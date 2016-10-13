
+++
date = "2016-01-27T16:14:21+08:00"
title = "Elastic-Job-Cloud-Scheduler启动指南"
weight=54
+++

# Elastic-Job-Cloud-Scheduler启动指南

1. 启动`Elastic-Job-Cloud-Scheduler`和`Mesos`指定注册中心的`Zookeeper`。

2. 启动`Mesos Master`和`Mesos Agent`。

3. 解压`elastic-job-cloud-scheduler-${version}.tar.gz`。可通过源码`mvn install`编译获取。

4. 执行`bin\start.sh`脚本启动`elastic-job-cloud-scheduler`。

# 附录

* 配置：修改`conf\elastic-job-cloud-scheduler.properties`文件。配置项说明如下：

| 属性名称                          | 必填     | 默认值                      | 描述                                                      |
| -------------------------------- |:--------|:----------------------------|:---------------------------------------------------------|
| hostname                         | `是`    |                             | 服务器真实的`IP`或`hostname`，不能是`127.0.0.1`或`localhost` |
| user                             | 否      |                             | `Mesos framework`使用的用户名称                            |
| mesos_url                        | `是`    | zk://127.0.0.1:2181/mesos   | `Mesos`所使用的`Zookeeper`地址                             |
| zk_servers                       | `是`    | 127.0.0.1:2181              | `Elastic-Job-Cloud`所使用的`Zookeeper`地址                 |
| zk_namespace                     | 否      | elastic-job-cloud           | `Elastic-Job-Cloud`所使用的`Zookeeper`命名空间              |
| zk_digest                        | 否      |                             | `Elastic-Job-Cloud`所使用的`Zookeeper`登录凭证              |
| http_port                        | `是`    | 8899                        | `Restful API`所使用的端口号                                 |
| app_cache_enable                 | `是`    | false                       | 每次执行作业时是否从缓存中读取应用。禁用则每次执行任务均从应用仓库下载应用至本地 |
| job_state_queue_size             | `是`    | 10000                       | 堆积作业最大值, 超过此阀值的堆积作业将直接丢弃。阀值过大可能会导致`Zookeeper`无响应，应根据实测情况调整 |

***

* 停止：不提供停止脚本，可直接使用`kill`杀进程。

* `Elastic-Job-Cloud-Scheduler`是什么？`Elastic-Job-Cloud-Scheduler`是`Mesos Framework`，用于资源调度和应用分发，需要独立启动提供服务。
