##Elastic-Job - distributed scheduled job solution

# [中文主页](README_cn.md)

[![Hex.pm](http://dangdangdotcom.github.io/elastic-job/img/license.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job)
[![Build Status](https://secure.travis-ci.org/dangdangdotcom/elastic-job.png?branch=master)](https://travis-ci.org/dangdangdotcom/elastic-job)
[![Coverage Status](https://coveralls.io/repos/dangdangdotcom/elastic-job/badge.svg?branch=master&service=github)](https://coveralls.io/github/dangdangdotcom/elastic-job?branch=master)

# Overview

Elastic-Job is a distributed scheduled job solution. Elastic-Job includes Elastic-Job-Lite and Elastic-Job-Cloud. 
Elastic-Job-Lite is a centreless solution, use lightweight jar to coordinate distributed jobs. Elastic-Job-Cloud is a Mesos framework which use Mesos + Docker to manage and isolate resources and processes.
Elastic-Job-Lite and Elastic-Job-Cloud provides same API. Developers  

# Features

## 1. Elastic-Job-Lite

* Distributed schedule job coordinate.
* Elastic scale in and scale out supported.
* Running jobs failover supported.
* Misfired jobs refire supported.
* Idempotency jobs execution supported.
* Parallel scheduling supported.
* Job lifecycle operation supported.
* Lavish job types supported.
* Spring integrated and namespace supported.
* Web console supported.

## 2. Elastic-Job-Cloud
* All features for Elastic-Job-Lite included.
* Resources allocated elastically.
* Resources distribute automatically.
* Docker based processes isolation supported (TBD).
* Maven assembly plugin supported.

***

# Architecture

## Elastic-job-Lite

![Elastic-Job-Lite Architecture](http://dangdangdotcom.github.io/elastic-job/img/architecture_lite_en.png)

## Elastic-Job-Cloud

![Elastic-Job-Cloud Architecture](http://dangdangdotcom.github.io/elastic-job/img/architecture_cloud_en.png)

# Quick Start

## Add maven dependency

```xml
<!-- import elastic-job core -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-core</artifactId>
    <version>${lasted.release.version}</version>
</dependency>

<!-- import other module if need -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-spring</artifactId>
    <version>${lasted.release.version}</version>
</dependency>
```
## Job development

```java
public class MyElasticJob extends AbstractSimpleElasticJob {
    
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // do something by sharding items
    }
}
```

## Job configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:reg="http://www.dangdang.com/schema/ddframe/reg"
    xmlns:job="http://www.dangdang.com/schema/ddframe/job"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.dangdang.com/schema/ddframe/reg
                        http://www.dangdang.com/schema/ddframe/reg/reg.xsd
                        http://www.dangdang.com/schema/ddframe/job
                        http://www.dangdang.com/schema/ddframe/job/job.xsd
                        ">
    <!--configure registry center -->
    <reg:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="dd-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
    
    <!--configure job -->
    <job:simple id="myElasticJob" class="xxx.MyElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?"   sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
</beans>
```
