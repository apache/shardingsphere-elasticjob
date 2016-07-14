
+++
date = "2016-01-27T16:14:21+08:00"
title = "部署指南"
weight=12
+++

# 部署指南

## Elastic-Job-Lite

1. 启动`Elastic-Job-Lite`指定注册中心的`Zookeeper`。

2. 部署运维平台`war`文件至任何支持`Servlet`的`Web`容器(可选)。
运维平台以`war`包形式提供，可自行部署至`tomcat`或`jetty`等支持`servlet`的`web`容器中。`elastic-job-console.war`可通过`mvn install`编译或`maven`中央仓库获取。

3. 运行包含`Elastic-Job-Lite`和业务代码的`jar`文件。

## Elastic-Job-Cloud

1. 启动`Elastic-Job-Cloud`和`Mesos`指定注册中心的`Zookeeper`。

2. 启动`Mesos Master`和`Mesos Agent`。

3. 解压并启动`elastic-job-cloud-master-${version}.tar.gz`。`elastic-job-cloud-master-${version}.tar.gz`可通过源码`mvn install`编译获取。

4. 配置`conf/job.properties`的作业`class`并使用`elastic-job-cloud-assembly`插件打包应用源码。更多打包插件信息请参考[使用指南](../other/cloud_assembly_plugin/)。

5. 将打包之后的作业`tar.gz`文件放至网络可访问的位置。如：`ftp`或`http`。

6. 使用curl命令注册待运行作业至`Elastic-Job-Cloud`。例：

`curl -l -H "Content-type: application/json" -X POST -d '{"jobName":"foo_job","cron":"0/5 * * * * ?","shardingTotalCount":5,"cpuCount":0.1,"memoryMB":64.0,"dockerImageName":"","appURL":"http://file_host:8080/foo-job.tar.gz","failover":false,"true":true}' http://elastic_job_cloud_masterhost:8899/job/register`

## 附录：Elastic-Job-Cloud-Master启动指南

1. 启动：解压缩`elastic-job-cloud-master-${version}.tar.gz`并执行`bin\start.sh`脚本。

2. 停止：不提供停止脚本，可直接使用`kill`杀进程。

3. 配置：修改`conf\elastic-job-cloud.properties`文件。配置项说明如下：

| 属性名称                          | 必填     | 默认值                      | 描述                                                      |
| -------------------------------- |:--------|:----------------------------|:---------------------------------------------------------|
| hostname                         | `是`    |                             | 服务器真实的`IP`或`hostname`，不能是`127.0.0.1`或`localhost` |
| username                         | 否      |                             | `Mesos framework`使用的用户名称                            |
| mesos_url                        | `是`    | `zk://127.0.0.1:2181/mesos` | `Mesos`所使用的`Zookeeper`地址                             |
| zk_servers                       | `是`    | `127.0.0.1:2181`            | `Elastic-Job-Cloud`所使用的`Zookeeper`地址                 |
| zk_namespace                     | 否      | `elastic-job-cloud`         | `Elastic-Job-Cloud`所使用的`Zookeeper`命名空间              |
| zk_digest                        | 否      |                             | `Elastic-Job-Cloud`所使用的`Zookeeper`登录凭证              |
| http_port                        | `是`    | `8899`                      | 作业操作的`Restful API`所使用的端口号                        |
