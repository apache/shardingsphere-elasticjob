+++
pre = "<b>4.1.2.1 </b>"
title = "注册中心配置"
weight = 1
chapter = true
+++

ElasticJob 支持多种注册中心类型，用于协调分布式作业的调度和执行。

本章节介绍如何配置不同类型的注册中心。

## 支持的注册中心类型

| 注册中心类型 | 说明 |
|-------------|------|
| [ZooKeeper](/cn/user-manual/configuration/registry-center/zookeeper) | Apache ZooKeeper，分布式协调服务 |
| [etcd](/cn/user-manual/configuration/registry-center/etcd) | etcd3，分布式键值存储 |
| [Nacos](/cn/user-manual/configuration/registry-center/nacos) | Nacos 3.x，动态服务发现与配置管理 |
| [Memory](/cn/user-manual/configuration/registry-center/memory) | 内置进程内实现，仅用于单元测试与本地开发 |

## 配置方式

上表中的每种注册中心文档都详细说明了其自身的配置属性与使用示例。

需要注意的是，Spring Boot Starter 与 Spring Namespace 目前仅支持 ZooKeeper。其他注册中心类型只能通过 Java API 或 `RegistryCenterFactory` 使用。
