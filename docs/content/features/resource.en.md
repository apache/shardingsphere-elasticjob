+++
pre = "<b>3.3. </b>"
title = "Resource Assign"
weight = 3
chapter = true
+++

The resource allocation function is unique to ElasticJob-Cloud.

## Execution mode

ElasticJob-Cloud is divided into two execution modes: transient and daemon execution.

### Transient execution

The resources are released immediately after the execution of each job to ensure that the existing resources are used for staggered execution.
Resource allocation and container startup both take up a certain amount of time, and resources may not be sufficient during job execution, so job execution may be delayed.
Transient execution is suitable for jobs with long intervals, high resource consumption and no strict requirements on execution time.

### Daemon execution

Whether it is running or waiting to run, it always occupies the allocated resources, which can save too many container startup and resource allocation costs, and is suitable for jobs with short intervals and stable resource requirements.

## Scheduler

ElasticJob-Cloud is developed based on the Mesos Framework and is used for resource scheduling and application distribution. It needs to be started independently and provides services.

## Job Application

Refers to the application after the job is packaged and deployed, and describes the basic information such as the CPU, memory, startup script, and application download path that are needed to start the job.
Each job application can contain one or more jobs.

## Job

That is, the specific tasks that are actually run share the same job ecology as ElasticJob-Lite.
The job application must be registered before registering the job.

## Resource

Refers to the CPU and memory required to start or run a job.
Configuration in the job application dimension indicates the resources needed for the entire application to start;
Configuration in the job dimension indicates the resources required for each job to run.
The resources required for job startup are the sum of the resources required by the specified job application and the resources required by the job.
