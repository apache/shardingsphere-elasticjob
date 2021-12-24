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

Configuration: 

| *Name*             | *Data Type*   | *Description*               | *Default Value*  |
| -----------------  | -----------   | --------------------------- | ---------------- |
| streaming.process  | boolean       | Enable or disable Streaming | false            |

### Script Job

Type: `SCRIPT`

Configuration: 

| *Name*               | *Data Type*   | *Description*           | *Default Value*  |
| -------------------- | ------------- | ----------------------- | ---------------- |
| script.command.line  | String        | Script content or path  | -                |

### HTTP Job

Type：`HTTP`

Configuration: 

| *Name*                             | *Data Type*    | *Description*          |  *Default Value*  |
| ---------------------------------- | -----------    | ----------------       | --------          |
| http.url                           | String         | http request url       | -                 |
| http.method                        | String         | http request method    | -                 |
| http.data                          | String         | http request data      | -                 |
| http.connect.timeout.milliseconds  | String         | http connect timeout   | 3000              |
| http.read.timeout.milliseconds     | String         | http read timeout      | 5000              |
| http.content.type                  | String         | http content type      | -                 |
