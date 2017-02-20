+++
date = "2017-01-07T16:14:21+08:00"
title = "Elastic-Job-Cloud运维平台"
weight=60
+++

# Elastic-Job-Cloud运维平台

运维平台内嵌于`elastic-job-cloud-scheduler`的jar包中, 无需额外启动WEB付服务器, 可通过修改配置文件中`http_port`参数来调整启动端口, 默认端口为8899，访问地址为`http://{your_scheduler_ip}:8899`。

## 功能列表

* 作业APP管理（发布、修改、查看）

* 作业管理（注册、修改、查看以及删除）

* 作业状态查看（待运行、运行中、待失效转移）

* 作业历史查看（运行轨迹、执行状态、历史dashboard）

## 设计理念

运维平台采用纯静态HTML+JS方式与后台的`RESTful API`交互，通过读取作业注册中心展示作业配置和状态，数据库展现作业运行轨迹及执行状态，或更新作业注册中心数据修改作业配置。
