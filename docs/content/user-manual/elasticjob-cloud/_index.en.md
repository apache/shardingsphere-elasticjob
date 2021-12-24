+++
pre = "<b>4.2. </b>"
title = "ElasticJob-Cloud"
weight = 2
chapter = true
+++

## Introduction

ElasticJob-Cloud uses Mesos to manage and isolate resources.

![ElasticJob-Cloud Architecture](https://shardingsphere.apache.org/elasticjob/current/img/architecture/elasticjob_cloud.png)

## Comparison

|                   | *ElasticJob-Lite* | *ElasticJob-Cloud*   |
| ----------------- | ----------------- | -------------------- |
| Decentralization  | Yes               | `No`                 |
| Resource Assign   | No                | `Yes`                |
| Job Execution     | Daemon            | `Daemon + Transient` |
| Deploy Dependency | ZooKeeper         | `ZooKeeper + Mesos`  |

The advantages of ElasticJob-Cloud are resource management and isolation, 
which is suitable for big data application with starve resource environment.
