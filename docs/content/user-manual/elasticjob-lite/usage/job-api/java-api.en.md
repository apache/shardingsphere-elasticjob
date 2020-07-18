+++
title = "Use Java API"
weight = 2
chapter = true
+++

## Job configuration

ElasticJob-Lite uses the builder mode to create job configuration objects.
The code example is as follows:

```java
    JobConfiguration jobConfig = JobConfiguration.newBuilder("myJob", 3).cron("0/5 * * * * ?").shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").build();
```

## Job start

ElasticJob-Lite scheduler is divided into two types: timed scheduling and one-time scheduling.
Each scheduler needs three parameters: registry configuration, job object (or job type), and job configuration when it starts.

### Timed scheduling

```java
public class JobDemo {
    
    public static void main(String[] args) {
        // Class-based Scheduling Jobs
        new ScheduleJobBootstrap(createRegistryCenter(), new MyJob(), createJobConfiguration()).schedule();
        // Type-based Scheduling Jobs
        new ScheduleJobBootstrap(createRegistryCenter(), "MY_TYPE", createJobConfiguration()).schedule();
    }
    
    private static CoordinatorRegistryCenter createRegistryCenter() {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration("zk_host:2181", "elastic-job-demo"));
        regCenter.init();
        return regCenter;
    }
    
    private static JobConfiguration createJobConfiguration() {
        // Create job configuration
        ...
    }
}
```

### One-time scheduling

```java
public class JobDemo {
    
    public static void main(String[] args) {
        OneOffJobBootstrap jobBootstrap = new OneOffJobBootstrap(createRegistryCenter(), new MyJob(), createJobConfiguration());
        // One-time scheduling can be called multiple times
        jobBootstrap.execute();
        jobBootstrap.execute();
        jobBootstrap.execute();
    }
    
    private static CoordinatorRegistryCenter createRegistryCenter() {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration("zk_host:2181", "elastic-job-demo"));
        regCenter.init();
        return regCenter;
    }
    
    private static JobConfiguration createJobConfiguration() {
        // Create job configuration
        ...
    }
}
```