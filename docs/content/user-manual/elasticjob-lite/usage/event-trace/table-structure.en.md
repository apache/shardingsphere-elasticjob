+++
title = "Table Structure"
weight = 4
chapter = true
+++

The database which is the value of the event tracing property `event_trace_rdb_url` will automatically creates two tables `JOB_EXECUTION_LOG` and `JOB_STATUS_TRACE_LOG` and several indexes.

## JOB_EXECUTION_LOG Columns

| Column name      | Column type   | Required  | Describe                                                                                |
| ---------------- |:------------- |:--------- |:--------------------------------------------------------------------------------------- |
| id               | VARCHAR(40)   | Yes       | Primary key                                                                             |
| job_name         | VARCHAR(100)  | Yes       | Job name                                                                                |
| task_id          | VARCHAR(1000) | Yes       | Task name, create new tasks every time the job runs.                                    |
| hostname         | VARCHAR(255)  | Yes       | Hostname                                                                                |
| ip               | VARCHAR(50)   | Yes       | IP                                                                                      |
| sharding_item    | INT           | Yes       | Sharding item                                                                           |
| execution_source | VARCHAR(20)   | Yes       | Source of job execution. The value options are `NORMAL_TRIGGER`, `MISFIRE`, `FAILOVER`. |
| failure_cause    | VARCHAR(2000) | No        | The reason for execution failure                                                        |
| is_success       | BIT           | Yes       | Execute successfully or not                                                             |
| start_time       | TIMESTAMP     | Yes       | Job start time                                                                          |
| complete_time    | TIMESTAMP     | No        | Job end time                                                                            |

`JOB_EXECUTION_LOG` records the execution history of each job.
There are two steps:

1. When the job is executed, program will create one record in the `JOB_EXECUTION_LOG`, and all fields except `failure_cause` and `complete_time` are not empty.
1. When the job completes execution, program will update the record, update the columns of `is_success`, `complete_time` and `failure_cause`(if the job execution fails).

## JOB_STATUS_TRACE_LOG Columns

| Column name      | Column type   | Required  | Describe                                                                                                                                                |
| ---------------- |:--------------|:----------|:------------------------------------------------------------------------------------------------------------------------------------------------------- |
| id               | VARCHAR(40)   | Yes       | Primary key                                                                                                                                             |
| job_name         | VARCHAR(100)  | Yes       | Job name                                                                                                                                                |
| original_task_id | VARCHAR(1000) | Yes       | Original task name                                                                                                                                      |
| task_id          | VARCHAR(1000) | Yes       | Task name                                                                                                                                               |
| slave_id         | VARCHAR(1000) | Yes       | Server's name of executing the job. The valve is server's IP for `ElasticJob-Lite`, is `Mesos`'s primary key for `ElasticJob-Cloud`.                    |
| source           | VARCHAR(50)   | Yes       | Source of job execution, the value options are `CLOUD_SCHEDULER`, `CLOUD_EXECUTOR`, `LITE_EXECUTOR`.                                                    |
| execution_type   | VARCHAR(20)   | Yes       | Type of job execution, the value options are `NORMAL_TRIGGER`, `MISFIRE`, `FAILOVER`.                                                                   |
| sharding_item    | VARCHAR(255)  | Yes       | Collection of sharding item, multiple sharding items are separated by commas.                                                                           |
| state            | VARCHAR(20)   | Yes       | State of job execution, the value options are `TASK_STAGING`, `TASK_RUNNING`, `TASK_FINISHED`, `TASK_KILLED`, `TASK_LOST`, `TASK_FAILED`, `TASK_ERROR`. |
| message          | VARCHAR(2000) | Yes       | Message                                                                                                                                                 |
| creation_time    | TIMESTAMP     | Yes       | Create time                                                                                                                                             |

`JOB_STATUS_TRACE_LOG` record the job status changes.
Through the `task_id` of each job, user can query the life cycle and running track of the job status change.
