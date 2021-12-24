+++
pre = "<b>4.2.2. </b>"
title = "Configuration"
weight = 2
chapter = true
+++

ElasticJob-Cloud provides RESTful APIs such as application publishing and job registration, which can be operated by curl.

Request URL prefix is `/api`

## Authentication API

### Get AccessToken

url: login

Method: POST

Content type: application/json

Parameter list:

| Property name           | Type    | Required or not | Default value  | Description                                                  |
| ----------------------- |:------- |:--------------- |:-------------- |:------------------------------------------------------------ |
| username                | String  | Yes             |                | API authentication username                                  |
| password                | String  | Yes             |                | API authentication password                                  |

Response parameter:

| Property name           | Type     | Description                                                  |
| ----------------------- |:-------  |:----------------------------- |
| accessToken             | String   | API authentication token      |

Example:

```bash
curl -H "Content-Type: application/json" -X POST http://elasticjob_cloud_host:8899/api/login -d '{"username": "root", "password": "pwd"}'
```

Response body:

```json
{"accessToken":"some_token"}
```


## Application API

### Publish application

url: app

Method: POST

Parameter type: application/json

Parameter list: 

| Property name           | Type    | Required or not | Default value  | Description                                                  |
| ----------------------- |:------- |:--------------- |:-------------- |:------------------------------------------------------------ |
| appName                 | String  | Yes             |                | Job application name                                         |
| appURL                  | String  | Yes             |                | Path of job application                                      |
| cpuCount                | double  | No              | 1              | The number of CPUs required for the job application to start |
| memoryMB                | double  | No              | 128            | MB of memory required to start the job application           |
| bootstrapScript         | String  | Yes             |                | Boot script                                                  |
| appCacheEnable          | boolean | No              | true           | Whether to read the application from the cache every time the job is executed |
| eventTraceSamplingCount | int     | No              | 0 (no sampling)| Number of resident job event sampling rate statistics        |

Detailed parameter description: 

**appName:**

It is the unique identifier of ElasticJob-Cloud's job application.

**appURL:**

A path that can be accessed through the network must be provided.

**bootstrapScript:**

Example: bin\start.sh

**appCacheEnable:**

Disabled, every time the task is executed, the application will be downloaded from the application repository to the local.

**eventTraceSamplingCount:**

To avoid excessive data volume, you can configure the sampling rate for frequently scheduled resident jobs, that is, every N times the job is executed, the job execution and tracking related data will be recorded.

Example: 

```bash
curl -l -H "Content-type: application/json" -X POST -d '{"appName":"my_app","appURL":"http://app_host:8080/my-job.tar.gz","cpuCount":0.1,"memoryMB":64.0,"bootstrapScript":"bin/start.sh","appCacheEnable":true,"eventTraceSamplingCount":0}' http://elastic_job_cloud_host:8899/api/app
```

### Modify application configuration

url: app

Method: PUT

Parameter type: application/json

Parameter list: 

| Property name           | Type    | Required or not | Default value      | Description                                          |
| ----------------------- |:------- |:--------------- |:------------------ |:---------------------------------------------------- |
| appName                 | String  | Yes             |                    | Job application name                                 |
| appCacheEnable          | boolean | Yes             | true               | Whether to read the application from the cache every time the job is executed |
| eventTraceSamplingCount | int     | No              | 0 (no sampling)    | Number of resident job event sampling rate statistics|

Example: 

```bash
curl -l -H "Content-type: application/json" -X PUT -d '{"appName":"my_app","appCacheEnable":true}' http://elastic_job_cloud_host:8899/api/app
```

## Job API

### Register job

url: job/register

Method: POST

Parameter type: application/json

Parameter list: 

| Property name                 | Type       | Required or not  | Default value  | Description                                                                            |
| ----------------------------- |:---------- |:---------------- |:-------------- |:-------------------------------------------------------------------------------------- |
| appName                       | String     | Yes              |                | Job application name                                                                   |
| cpuCount                      | double     | Yes              |                | The number of CPUs required for a single chip operation, the minimum value is 0.001    |
| memoryMB                      | double     | Yes              |                | The memory MB required for a single chip operation, the minimum is 1                   |
| jobExecutionType              | Enum       | Yes              |                | Job execution type. TRANSIENT is a transient operation, DAEMON is a resident operation |
| jobName                       | String     | Yes              |                | Job name                                                                               |
| cron                          | String     | No               |                | cron expression, used to configure job trigger time                                    |
| shardingTotalCount            | int        | Yes              |                | Total number of job shards                                                             |
| shardingItemParameters        | String     | No               |                | Custom sharding parameters                                                             |
| jobParameter                  | String     | No               |                | Job custom parameters                                                                  |
| failover                      | boolean    | No               | false          | Whether to enable failover                                                             |
| misfire                       | boolean    | No               | false          | Whether to enable missed tasks to re-execute                                           |
| jobExecutorServiceHandlerType | boolean    | No               | false          | Job thread pool processing strategy                                                    |
| jobErrorHandlerType           | boolean    | No               | false          | Job error handling strategy                                                            |
| description                   | String     | No               |                | Job description information                                                            |
| props                         | Properties | No               |                | Job property configuration information                                                 |

Use the script type instantaneous job to upload the script directly to appURL without tar package.
If there is only a single script file, no compression is required.
If it is a complex script application, you can still upload a tar package and support various common compression formats.

Example: 

```bash
curl -l -H "Content-type: application/json" -X POST -d '{"appName":"my_app","cpuCount":0.1,"memoryMB":64.0,"jobExecutionType":"TRANSIENT","jobName":"my_job","cron":"0/5 * * * * ?","shardingTotalCount":5,"failover":true,"misfire":true}' http://elastic_job_cloud_host:8899/api/job/register
```

### update job configuration

url: job/update

Method: PUT

Parameter type: application/json

Parameters: same as registration job

Example: 

```bash
curl -l -H "Content-type: application/json" -X PUT -d '{"appName":"my_app","jobName":"my_job","cpuCount":0.1,"memoryMB":64.0,"jobExecutionType":"TRANSIENT","cron":"0/5 * * * * ?","shardingTotalCount":5,"failover":true,"misfire":true}' http://elastic_job_cloud_host:8899/api/job/update
```

### Deregister Job

url: job/deregister

Method: DELETE

Parameter type: application/json

Parameters: Job name

Example: 

```bash
curl -l -H "Content-type: application/json" -X DELETE -d 'my_job' http://elastic_job_cloud_host:8899/api/job/deregister
```

### Trigger job

url: job/trigger

Method: POST

Parameter type: application/json

Parameters: Job name

Description: Event-driven, triggering jobs by calling API instead of timing. Currently only valid for transient operations.

Example: 

```bash
curl -l -H "Content-type: application/json" -X POST -d 'my_job' http://elastic_job_cloud_host:8899/api/job/trigger
```
