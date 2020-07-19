+++
pre = "<b>5.4. </b>"
title = "Roadmap"
weight = 4
chapter = true
+++

## Core

- [x] Unified Job Config API
    - [x] Core Config
    - [x] Type Config
    - [x] Root Config
- [x] Job Types
    - [x] Simple
    - [x] Dataflow
    - [x] Script
    - [ ] Http
- [x] Event Trace
    - [x] Event Publisher
    - [x] Database Event Listener
    - [ ] Other Event Listener
- [ ] Unified Schedule API
- [ ] Unified Resource API

## ElasticJob-Lite

- [x] Distributed Features
    - [x] High Availability
    - [x] Elastic scale in/out
    - [x] Failover
    - [x] Misfire
    - [x] Idempotency
    - [x] Reconcile
- [x] Registry Center
    - [x] Zookeeper
    - [ ] Other Registry Center Supported
- [x] Lifecycle Management
    - [x] Add/Remove
    - [x] Pause/Resume
    - [x] Disable/Enable
    - [x] Shutdown
    - [x] Restful API
    - [x] Web Console
- [x] Job Dependency
    - [x] Listener
    - [ ] DAG
- [x] Spring Integrate
    - [x] Namespace
    - [x] Bean Injection
    - [ ] Spring Boot Starter

## ElasticJob-Cloud
- [x] Transient Job
    - [x] High Availability
    - [x] Elastic scale in/out
    - [x] Failover
    - [x] Misfire
    - [x] Idempotency
- [x] Daemon Job
    - [x] High Availability
    - [x] Elastic scale in/out
    - [ ] Failover
    - [ ] Misfire
    - [x] Idempotency
- [x] Mesos Scheduler
    - [x] High Availability
    - [x] Reconcile
    - [ ] Redis Based Queue Improvement
    - [ ] Http Driver
- [x] Mesos Executor
    - [x] Executor Reuse Pool
    - [ ] Progress Reporting
    - [ ] Health Detection
    - [ ] Log Redirect
- [x] Lifecycle Management
    - [x] Job Add/Remove
    - [ ] Job Pause/Resume
    - [x] Job Disable/Enable
    - [ ] Job Shutdown
    - [x] App Add/Remove
    - [x] App Disable/Enable
    - [x] Restful API
    - [x] Web Console
- [ ] Job Dependency
    - [ ] Listener
    - [ ] Workflow
    - [ ] DAG
- [x] Job Distribution
    - [x] Mesos Based Distribution
    - [ ] Docker Based Distribution
- [x] Resources Management
    - [x] Resources Allocate
    - [ ] Cross Data Center
    - [ ] A/B Test
- [x] Spring Integrate
    - [x] Bean Injection
