+++
pre = "<b>3.1. </b>"
title = "Schedule Model"
weight = 1
chapter = true
+++

Unlike most job platforms, ElasticJob's scheduling model is divided into in-process scheduling ElasticJob-Lite that supports thread-level scheduling, and ElasticJob-Cloud for process-level scheduling.

## In-process scheduling

ElasticJob-Lite is a thread-level scheduling framework for in-process. Through it, Job can be transparently combined with business application systems.
It can be easily used in conjunction with Java frameworks such as Spring and Dubbo. Spring DI (Dependency Injection) Beans can be freely used in Job, such as data source connection pool and Dubbo remote service, etc., which is more convenient for business development.

## Process-level scheduling

ElasticJob-Cloud has two methods: in-process scheduling and process-level scheduling.
Because ElasticJob-Cloud can control the resources of the job server, its job types can be divided into resident tasks and transient tasks.
The resident task is similar to ElasticJob-Lite, which is an in-process scheduling; the transient task is completely different. It fully utilizes the peak-cutting and valley-filling capabilities of resource allocation, and is a process-level scheduling. Each task will start a new process.