+++
date = "2017-01-07T16:14:21+08:00"
title = "Elastic-Job-Cloud运维平台"
weight=21
+++

# Elastic-Job-Cloud运维平台

运维平台内嵌于`elastic-job-cloud-scheduler`的jar包中, 无需额外启动WEB付服务器, 可通过修改配置文件中`http_port`参数来调整启动端口, 默认端口为8899，访问地址为`http://{your_scheduler_ip}:8899`。

## 功能列表

* 作业管理（注册、修改、查看以及删除）

* 作业状态查看（待运行、运行中、待失效转移）

* 作业历史查看（运行轨迹、执行状态、历史dashboard）

## 设计理念

运维平台采用纯静态HTML+JS方式与后台的`Restful API`交互，通过读取作业注册中心展示作业配置和状态，数据库展现作业运行轨迹及执行状态，或更新作业注册中心数据修改作业配置。

## 主要界面

* 作业定义

![作业定义](../../../../../img/console/cloud/job/job_def.png)
![作业注册](../../../../../img/console/cloud/job/job_reg.png)
![作业详情](../../../../../img/console/cloud/job/job_detail.png)
![作业修改](../../../../../img/console/cloud/job/job_update.png)
![作业删除](../../../../../img/console/cloud/job/job_remove.png)

* 作业状态

![待运行作业状态](../../../../../img/console/cloud/status/job_ready.png)
![运行中作业状态](../../../../../img/console/cloud/status/job_running.png)
![待失效转移任务状态](../../../../../img/console/cloud/status/job_failover.png)

* 作业历史

![运行轨迹查询](../../../../../img/console/cloud/history/job_status_trace.png)
![执行状态查询](../../../../../img/console/cloud/history/job_execution.png)
![历史dashboard](../../../../../img/console/cloud/history/dashboard.png)
