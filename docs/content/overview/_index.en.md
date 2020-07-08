+++
pre = "<b>1. </b>"
title = "Overview"
weight = 1
chapter = true
+++

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere-elasticjob-lite.svg?style=social&label=Release)](https://github.com/apache/shardingsphere-elasticjob-lite/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/apache/shardingsphere-elasticjob-lite.svg?style=social&label=Star)](https://github.com/apache/shardingsphere-elasticjob-lite/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/apache/shardingsphere-elasticjob-lite.svg?style=social&label=Fork)](https://github.com/apache/shardingsphere-elasticjob-lite/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/apache/shardingsphere-elasticjob-lite.svg?style=social&label=Watch)](https://github.com/apache/shardingsphere-elasticjob-lite/watchers)
[![Stargazers over time](https://starchart.cc/apache/shardingsphere-elasticjob-lite.svg)](https://starchart.cc/apache/shardingsphere-elasticjob-lite)

ElasticJob is a distributed scheduled job solution. 
It is composited from 2 independent sub projects: ElasticJob Lite and ElasticJob Cloud.

ElasticJob Lite is a centre-less solution, use lightweight jar to coordinate distributed jobs.

ElasticJob Lite and ElasticJob Cloud provide unified API. 
Developers only need code one time, then decide to deploy Lite or Cloud as you want.

ElasticJob became an [Apache ShardingSphere](https://shardingsphere.apache.org/) Sub project on May 28 2020.

Welcome communicate with community via [mail list](mailto:dev@shardingsphere.apache.org).

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere-elasticjob-lite.svg)](https://github.com/apache/shardingsphere-elasticjob-lite/releases)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/elastic-job)
[![Build Status](https://secure.travis-ci.org/apache/shardingsphere-elasticjob-lite.png?branch=master)](https://travis-ci.org/apache/shardingsphere-elasticjob-lite)
[![Coverage Status](https://coveralls.io/repos/elasticjob/elastic-job/badge.svg?branch=master&service=github)](https://coveralls.io/github/elasticjob/elastic-job?branch=master)

## Architecture

### Elastic-Job-Lite

![Elastic-Job-Lite Architecture](docs/static/img/architecture/elastic_job_lite.png)

## Features

* Distributed schedule job coordinate
* Elastic scale in and scale out supported
* Failover
* Misfired jobs refire
* Sharding consistently, same sharding item for a job only one running instance
* Self diagnose and recover when distribute environment unstable
* Parallel scheduling supported
* Job lifecycle operation
* Lavish job types
* Spring integrated and namespace supported
* Web console
