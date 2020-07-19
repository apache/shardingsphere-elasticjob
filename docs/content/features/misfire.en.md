+++
pre = "<b>3.5. </b>"
title = "Misfire"
weight = 5
chapter = true
+++

ElasticJob does not allow jobs to be executed at the same time.
When the execution time of a job exceeds its running interval, re-executing the missed task can ensure that the job continues to execute the overdue job after completing the last task.

## Concept

The misfire function enables the overdue tasks to be executed immediately after the completion of the previous tasks.
For example, if the job is executed at an hourly interval, each execution will take 30 minutes. As shown below.

![Job](https://shardingsphere.apache.org/elasticjob/current/img/misfire/job.png)

The figure shows that the jobs are executed at 12:00, 13:00 and 14:00 respectively. The current time point shown in the figure is the job execution at 13:00.

If the job executed at 12:00 is finished at 13:10, then the job that should have been triggered by 13:00 missed the trigger time and needs to wait until the next job trigger at 14:00. As shown below.

![Job Missed](https://shardingsphere.apache.org/elasticjob/current/img/misfire/job-missed.png)

After the misfire is enabled, ElasticJob will trigger the execution of the missed job immediately after the last job is executed. As shown below.

![Job Misfire](https://shardingsphere.apache.org/elasticjob/current/img/misfire/job-misfire.png)

Missed jobs between 13:00 and 14:00 will be executed again.

## Scenarios

In a job scenario that takes a long time to run and has a long interval, misfire is an effective means to improve the real-time operation of the job;
For short-interval jobs that do not necessarily pay attention to the real-time performance of a single job, it is not necessary to turn on the misfire to re-execute.
