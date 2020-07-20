+++
title = "Local Executor"
weight = 2
chapter = true
+++

When developing ElasticJob-Cloud jobs, developers can leave the Mesos environment to run and debug jobs locally.
The local operating mode can be used to fully debug business functions and unit tests, and then deploy to the Mesos cluster after completion.

There is no need to install the Mesos environment to run jobs locally.

```java
// Create job configuration
JobConfiguration jobConfig = JobConfiguration.newBuilder("myJob", 3).cron("0/5 * * * * ?").build();

// Configure the fragmentation item of the currently running job
int shardingItem = 0;

// Create a local executor
new LocalTaskExecutor(new MyJob(), jobConfig, shardingItem).execute();
```
