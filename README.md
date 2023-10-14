# [ElasticJob - Distributed scheduled job](http://shardingsphere.apache.org/elasticjob/)

**Official website: https://shardingsphere.apache.org/elasticjob/**

[![Stargazers over time](https://starchart.cc/apache/shardingsphere-elasticjob.svg)](https://starchart.cc/apache/shardingsphere-elasticjob)

Through the functions of flexible scheduling, resource management and job management, 
it creates a distributed scheduling solution suitable for Internet scenarios, 
and provides a diversified job ecosystem through open architecture design.
It uses a unified job API for each project.
Developers only need code one time and can deploy at will.

ElasticJob became an [Apache ShardingSphere](https://shardingsphere.apache.org/) Sub-project on May 28 2020.

You are welcome to communicate with the community via the [mailing list](mailto:dev@shardingsphere.apache.org).

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere-elasticjob.svg)](https://github.com/apache/shardingsphere-elasticjob/releases)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/org.apache.shardingsphere.elasticjob/elasticjob/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.shardingsphere.elasticjob/elasticjob)
[![Build Status](https://secure.travis-ci.org/apache/shardingsphere-elasticjob.png?branch=master)](https://travis-ci.org/apache/shardingsphere-elasticjob)
[![GitHub Workflow](https://img.shields.io/github/workflow/status/apache/shardingsphere-elasticjob/Java%20CI%20with%20Maven%20on%20macOS/master)](https://github.com/apache/shardingsphere-elasticjob/actions?query=workflow%3A%22Java+CI+with+Maven+on+macOS%22)
[![codecov](https://codecov.io/gh/apache/shardingsphere-elasticjob/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/shardingsphere-elasticjob)
[![Maintainability](https://cloud.quality-gate.com/dashboard/api/badge?projectName=apache_shardingsphere-elasticjob&branchName=master)](https://cloud.quality-gate.com/dashboard/branches/396041#overview)

## Introduction

Using ElasticJob developers can no longer worry about the non functional requirements such as job scale out, so that they can focus more on business coding.
At the same time, it can release operators too, so that they do not have to worry about high availability and management, and can automatically operate by simply adding servers.

It is a lightweight, decentralized solution that provides distributed task sharding services.

![ElasticJob Architecture](https://shardingsphere.apache.org/elasticjob/current/img/architecture/elasticjob_lite.png)

## Features

- Elastic Schedule
  - Support job sharding and high availability in distributed system
  - Scale out for throughput and efficiency improvement
  - Job processing capacity is flexible and scalable with the allocation of resources

- Resource Assign
  - Execute job on suitable time and assigned resources
  - Aggregation same job to same job executor
  - Append resources to newly assigned jobs dynamically

- Job Governance
  - Failover
  - Misfired
  - Self diagnose and recover when distribute environment unstable

- Job Dependency (TODO)
  - DAG based job dependency
  - DAG based job item dependency

- Job Open Ecosystem
  - Unify job api for extension
  - Support rich job type lib, such as dataflow, script, HTTP, file, big data
  - Focus business SDK, can work with Spring IOC

- [Admin Console](https://github.com/apache/shardingsphere-elasticjob-ui)
  - Job administration
  - Job event trace query
  - Registry center management

## Environment Required

### Java

Java 8 or above required.

### Maven

Maven 3.5.0 or above required.

### ZooKeeper

ZooKeeper 3.6.0 or above required. [See details](https://zookeeper.apache.org/)
