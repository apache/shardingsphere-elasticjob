+++
pre = "<b>4.1. </b>"
title = "ElasticJob-Lite"
weight = 1
chapter = true
+++

## Introduction

ElasticJob-Lite is a lightweight, decentralized solution that provides distributed task sharding services.

![ElasticJob-Lite Architecture](https://shardingsphere.apache.org/elasticjob/current/img/architecture/elasticjob_lite.png)

## Comparison

|                   | *ElasticJob-Lite* | *ElasticJob-Cloud* |
| ----------------- | ----------------- | ------------------ |
| Decentralization  | `Yes`             | No                 |
| Resource Assign   | `No`              | Yes                |
| Job Execution     | `Daemon`          | Daemon + Transient |
| Deploy Dependency | `ZooKeeper`       | ZooKeeper + Mesos  |

The advantages of ElasticJob-Lite are no centralized design and less external dependence, 
which is suitable for business application with stable resource allocation.
