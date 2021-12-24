+++
pre = "<b>3.1. </b>"
title = "调度模型"
weight = 1
chapter = true
+++

与大部分的作业平台不同，ElasticJob 的调度模型划分为支持线程级别调度的进程内调度 ElasticJob-Lite，和进程级别调度的 ElasticJob-Cloud。

## 进程内调度

ElasticJob-Lite 是面向进程内的线程级调度框架。通过它，作业能够透明化的与业务应用系统相结合。
它能够方便的与 Spring 、Dubbo 等 Java 框架配合使用，在作业中可自由使用 Spring 注入的 Bean，如数据源连接池、Dubbo 远程服务等，更加方便的贴合业务开发。

## 进程级调度

ElasticJob-Cloud 拥有进程内调度和进程级别调度两种方式。
由于 ElasticJob-Cloud 能够对作业服务器的资源进行控制，因此其作业类型可划分为常驻任务和瞬时任务。
常驻任务类似于 ElasticJob-Lite，是进程内调度；瞬时任务则完全不同，它充分的利用了资源分配的削峰填谷能力，是进程级的调度，每次任务的会启动全新的进程处理。
