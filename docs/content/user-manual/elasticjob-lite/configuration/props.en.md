+++
title = "Job Properties"
weight = 5
chapter = true
+++

## Introduction

`ElasticJob` provide customized configurations for different types of jobs through the way of attribute configuration.

## Job Type

### Simple Job

Interface name: `org.apache.shardingsphere.elasticjob.simple.job.SimpleJob`

Configuration: no

### Dataflow Job

Interface name: `org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob`

Configuration：

| *Name*             | *Data Type*   | *Description*               | *Default Value*  |
| -----------------  | -----------   | --------------------------- | ---------------- |
| streaming.process  | boolean       | Enable or disable Streaming | false            |

### Script Job

Type: `SCRIPT`

Configuration: 

| *Name*               | *Data Type*   | *Description*           | *Default Value*  |
| -------------------- | ------------- | ----------------------- | ---------------- |
| script.command.line  | String        | Script content or path  | -                |
