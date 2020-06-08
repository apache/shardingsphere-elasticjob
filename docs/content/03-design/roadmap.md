+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "未来线路规划"
weight = 4
prev = "/03-design/lite-design/"
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

## Elastic-Job-Lite
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
    - [ ] Workflow
    - [ ] DAG
- [x] Spring Integrate
    - [x] Namespace
    - [x] Bean Injection