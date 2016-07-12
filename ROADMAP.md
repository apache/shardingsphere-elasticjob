# Roadmap

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


## Elastic-Job-Lite
- [x] Distributed Features
    - [x] High Availability
    - [x] LeaderShip Election
    - [x] Elastic scale in/out
    - [x] Failover
    - [x] Misfire
    - [x] Idempotency
- [x] Registry Center
    - [x] Zookeeper
    - [ ] Health Detection
- [x] Lifecycle Operation
    - [x] Add/Remove
    - [x] Pause/Resume
    - [x] Disable/Enable
    - [x] Shutdown
    - [ ] Restful API
- [x] Job Dependency
    - [x] Listener
    - [ ] Workflow
- [x] Job Types
    - [x] Simple
    - [x] Data Flow
    - [x] Script
    - [ ] Http
- [x] Spring Integrate
- [x] Web Console

## Elastic-Job-Cloud
- [x] Job Distributed Features
    - [X] High Availability
    - [x] Elastic scale in/out
    - [x] Failover
    - [x] Misfire
    - [x] Idempotency
- [x] Mesos Framework Distributed Features
    - [ ] High Availability
    - [ ] LeaderShip Election
- [x] Registry Center
    - [x] Zookeeper
    - [ ] Health Detection
- [x] Lifecycle Operation
    - [x] Add/Remove
    - [ ] Pause/Resume
    - [ ] Disable/Enable
    - [x] Shutdown
    - [x] Restful API
- [ ] Job Dependency
    - [ ] Listener
    - [ ] Workflow
- [x] Job Types
    - [x] Simple
    - [ ] Data Flow
    - [ ] Script
    - [ ] Http
- [x] Job Distribution
    - [x] Mesos Based Distribution
    - [ ] Docker Based Distribution
- [x] Resource Management
    - [x] Resources Allocate
    - [x] Transient Job
    - [ ] Daemon Job
- [ ] Spring Integrate
- [ ] Web Console

## Common
- [ ] Unify Job API
- [ ] Monitor
    - [ ] Events Database
    - [ ] ELK Integrate
