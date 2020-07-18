+++
title = "Use Java API"
weight = 2
chapter = true
+++

## Common Listener

```java
public class JobMain {
    
    public static void main(String[] args) {
        new ScheduleJobBootstrap(createRegistryCenter(), createJobConfiguration(), new MyElasticJobListener()).schedule();
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

## Distributed Listener

```java
public class JobMain {
    
    public static void main(String[] args) {
        long startTimeoutMills = 5000L;
        long completeTimeoutMills = 10000L;
        new ScheduleJobBootstrap(createRegistryCenter(), createJobConfiguration(), new MyDistributeOnceElasticJobListener(startTimeoutMills, completeTimeoutMills)).schedule();
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
