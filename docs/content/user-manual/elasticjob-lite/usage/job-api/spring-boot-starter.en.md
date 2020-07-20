+++
title = "Use Spring Boot Starter"
weight = 3
chapter = true
+++

ElasticJob-Lite provides a customized Spring Boot Starter, which can be used in conjunction with Spring Boot.
Developers are free from configuring CoordinatorRegistryCenter, JobBootstrap by using ElasticJob Spring Boot Starter.
What developers need to solve distributed scheduling problem are job implementations with a little configuration.

## Job configuration

### Implements ElasticJob

Job implementation is similar to other usage of ElasticJob. 
The difference is that jobs will be registered into the Spring IoC container.

**Thread-Safety Issue**

Bean is singleton by default. 
Consider setting Bean Scope to `prototype` if the instance of ElasticJob would be used by more than a JobBootstrap.

```java
@Component
public class SpringBootDataflowJob implements DataflowJob<Foo> {
    
    @Override
    public List<Foo> fetchData(final ShardingContext shardingContext) {
        // fetch data
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<Foo> data) {
        // process data
    }
}
```

### Configure CoordinateRegistryCenter and Jobs

Configure the Zookeeper which will be used by ElasticJob via configuration files.

Those jobs which have implemented ElasticJob are `classed` job. 
They should be configured under `elasticjob.jobs.classed`.
`elasticjob.jobs.classed` is a Map which using qualified class name as keys and List<JobConfigurationPOJO> as values.
The Starter will create instances of `OneOffJobBootstrap` or `ScheduleJobBootstrap` and register them into the Spring IoC container automatically. 

Configuration reference:

```yaml
elasticjob:
  regCenter:
    serverLists: localhost:6181
    namespace: elasticjob-lite-springboot
  jobs:
    classed:
      org.apache.shardingsphere.elasticjob.example.job.SpringBootDataflowJob:
        - jobName: dataflowJob
          cron: 0/5 * * * * ?
          shardingTotalCount: 3
          shardingItemParameters: 0=Beijing,1=Shanghai,2=Guangzhou
    typed:
      SCRIPT:
        - jobName: scriptJob
          cron: 0/10 * * * * ?
          shardingTotalCount: 3
          props:
            script.command.line: "echo SCRIPT Job: "
```

## Job Start

### Schedule Job

Just start Spring Boot Starter directly. The schedule jobs will startup when the Spring Boot Application is started.

### One-off Job

When to execute OneOffJob is up to you. 
Developers can inject the `OneOffJobBootstrap` bean into where they plan to invoke.
Trigger the job by invoking `execute()` method manually.

**About @DependsOn Annotation**

JobBootstraps are created by the Starter dynamically. It's unable to inject the `JobBootstrap` beans if the beans which depends on `JobBootstrap` were instantiated earlier than the instantiation of `JobBootstrap`.

Developers can also retrieve `JobBootstrap` beans by ApplicationContext.

```java
@RestController
@DependsOn("ElasticJobLiteAutoConfiguration")
public class OneOffJobController {
    
    @Resource(name = "manualScriptJobOneOffJobBootstrap")
    private OneOffJobBootstrap manualScriptJob;
    
    @GetMapping("/execute")
    public String executeOneOffJob() {
        manualScriptJob.execute();
        return "{\"msg\":\"OK\"}";
    }
}
```
