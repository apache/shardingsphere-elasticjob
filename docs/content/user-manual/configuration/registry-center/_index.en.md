+++
pre = "<b>4.1.2.1 </b>"
title = "Registry Center Configuration"
weight = 1
chapter = true
+++

ElasticJob supports multiple types of registry centers for coordinating the scheduling and execution of distributed jobs.

This section describes how to configure different types of registry centers.

## Supported Registry Center Types

| Registry Center Type | Description |
|---------------------|-------------|
| [ZooKeeper](/en/user-manual/configuration/registry-center/zookeeper) | Apache ZooKeeper, distributed coordination service |
| [etcd](/en/user-manual/configuration/registry-center/etcd) | etcd3, distributed key-value store |
| [Nacos](/en/user-manual/configuration/registry-center/nacos) | Nacos 3.x, dynamic service discovery and configuration management |
| [Memory](/en/user-manual/configuration/registry-center/memory) | Built-in in-process implementation, for unit tests and local development only |

## How to Configure

The document of each registry center type above describes its own configuration properties and usage examples in detail.

Please note that the Spring Boot Starter and the Spring Namespace currently only support ZooKeeper. Other registry center types can only be used through the Java API or `RegistryCenterFactory`.
