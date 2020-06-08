+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "未来线路规划"
weight = 4
prev = "/03-design/module/"
next = "/00-overview"
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

## Elastic-Job-Cloud
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
