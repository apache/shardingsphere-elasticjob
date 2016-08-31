
+++
date = "2016-01-27T16:14:21+08:00"
title = "部署指南"
weight=12
+++

# 部署指南

## Elastic-Job-Cloud

1. 启动`Elastic-Job-Cloud`和`Mesos`指定注册中心的`Zookeeper`。

2. 启动`Mesos Master`和`Mesos Agent`。

3. 解压并启动`elastic-job-cloud-scheduler-${version}.tar.gz`。可通过源码`mvn install`编译获取。

4. 将打包之后的作业`tar.gz`文件放至网络可访问的位置，如：`ftp`或`http`。打包的`tar.gz`文件中`Main`方法需要调用`Elastic-Job-Lite`提供的`Bootstrap.execute`方法。

5. 使用curl命令注册待运行作业至`Elastic-Job-Cloud`。

`curl -l -H "Content-type: application/json" -X POST -d 
'{"jobName":"foo_job","jobClass":"yourJobClass","jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"appURL":"http://file_host:8080/foo-job.tar.gz","failover":false,"misfire":true} 
http://elastic_job_cloud_masterhost:8899/job/register`

## 附录：Elastic-Job-Cloud-Master启动指南

1. 启动：解压缩`elastic-job-cloud-scheduler-${version}.tar.gz`并执行`bin\start.sh`脚本。

2. 停止：不提供停止脚本，可直接使用`kill`杀进程。

3. 配置：修改`conf\elastic-job-cloud.properties`文件。配置项说明如下：

| 属性名称                          | 必填     | 默认值                      | 描述                                                      |
| -------------------------------- |:--------|:----------------------------|:---------------------------------------------------------|
| hostname                         | `是`    |                             | 服务器真实的`IP`或`hostname`，不能是`127.0.0.1`或`localhost` |
| user                             | 否      |                             | `Mesos framework`使用的用户名称                            |
| mesos_url                        | `是`    | `zk://127.0.0.1:2181/mesos` | `Mesos`所使用的`Zookeeper`地址                             |
| zk_servers                       | `是`    | `127.0.0.1:2181`            | `Elastic-Job-Cloud`所使用的`Zookeeper`地址                 |
| zk_namespace                     | 否      | `elastic-job-cloud`         | `Elastic-Job-Cloud`所使用的`Zookeeper`命名空间              |
| zk_digest                        | 否      |                             | `Elastic-Job-Cloud`所使用的`Zookeeper`登录凭证              |
| http_port                        | `是`    | `8899`                      | 作业操作的`Restful API`所使用的端口号                        |
