+++
toc = true
date = "2017-02-20T10:38:50+08:00"
title = "核心概念"
weight = 1
prev = "/02-guide"
next = "/02-guide/cloud-web-console/"
+++

本文介绍Elastic-Job-Cloud包含的一些核心概念。

## Elastic-Job-Cloud-Scheduler
Elastic-Job-Cloud-Scheduler表示基于Mesos的Framework，用于资源调度和应用分发，需要独立启动并提供服务。

## 作业APP
作业APP指作业打包部署后的应用，描述了作业启动需要用到的CPU、内存、启动脚本及应用下载路径等基本信息，每个APP可以包含一个或多个作业。

## 作业
作业即实际运行的具体任务，和Lite版本一样，包含Simple、Dataflow和Script共3种作业类型，在注册作业前必须先注册作业APP。

## 资源
资源指作业启动或运行需要用到的CPU、内存，配置在APP维度表示整个应用启动需要用的资源，配置在JOB维度表示每个作业运行需要的资源。
