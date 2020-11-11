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

`elasticjob.jobs` is a Map. Using key as job name. Specific job type and configuration in value.
The Starter will create instances of `OneOffJobBootstrap` or `ScheduleJobBootstrap` and register them into the Spring IoC container automatically. 

Configuration reference:

```yaml
elasticjob:
  regCenter:
    serverLists: localhost:6181
    namespace: elasticjob-lite-springboot
  jobs:
    dataflowJob:
      elasticJobClass: org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob
      cron: 0/5 * * * * ?
      shardingTotalCount: 3
      shardingItemParameters: 0=Beijing,1=Shanghai,2=Guangzhou
    scriptJob:
      elasticJobType: SCRIPT
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

The bean name of `OneOffJobBootstrap` is specified by property "jobBootstrapBeanName",
Please refer to [Spring Boot Starter Configuration](/en/user-manual/elasticjob-lite/configuration/spring-boot-starter).

```yaml
elasticjob:
  jobs:
    myOneOffJob:
      jobBootstrapBeanName: myOneOffJobBean
      ....
```

```java
@RestController
public class OneOffJobController {

    // Inject via "@Resource"
    @Resource(name = "myOneOffJobBean")
    private OneOffJobBootstrap myOneOffJob;
    
    @GetMapping("/execute")
    public String executeOneOffJob() {
        myOneOffJob.execute();
        return "{\"msg\":\"OK\"}";
    }

    // Inject via "@Autowired"
    @Autowired
    @Qualifier(name = "myOneOffJobBean")
    private OneOffJobBootstrap myOneOffJob2;

    @GetMapping("/execute2")
    public String executeOneOffJob2() {
        myOneOffJob2.execute();
        return "{\"msg\":\"OK\"}";
    }
}
```

## Configuration error handler strategy

In the process of using ElasticJob-Lite, when the job is abnormal, the following error handling strategies can be used.

| *Error handler strategy name*            | *Description*                                                 |  *Built-in*  | *Default*| *Extra config*   |
| ---------------------------------------- | ------------------------------------------------------------- |  -------     |  --------|  --------------  |
| Log Strategy                             | Log error and do not interrupt job                            |   Yes        |     Yes  |                  |
| Throw Strategy                           | Throw system exception and interrupt job                      |   Yes        |          |                  |
| Ignore Strategy                          | Ignore exception and do not interrupt job                     |   Yes        |          |                  |
| Email Notification Strategy              | Send email message notification and do not interrupt job      |              |          |    Yes           |
| Wechat Enterprise Notification Strategy  | Send wechat message notification and do not interrupt job     |              |          |    Yes           |
| Dingtalk Notification Strategy           | Send dingtalk message notification and do not interrupt job   |              |          |    Yes           |

### Log Strategy
```yaml
elasticjob:
  regCenter:
    ...
  jobs:
    ...
    jobErrorHandlerType: LOG 
```

### Throw Strategy
```yaml
elasticjob:
  regCenter:
    ...
  jobs:
    ...
    jobErrorHandlerType: THROW 
```

### Ignore Strategy
```yaml
elasticjob:
  regCenter:
    ...
  jobs:
    ...
    jobErrorHandlerType: IGNORE 
```

### Email Notification Strategy

Please refer to [here](/en/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler/#email-notification-strategy) for more details.

Maven POM:
```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-email</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```
```yaml
elasticjob:
  regCenter:
    ...
  jobs:
    ...
    jobErrorHandlerType: EMAIL 
  props:
    email:
      host: host
      port: 465
      username: username
      password: password
      useSsl: true
      subject: ElasticJob error message
      from: from@xxx.xx
      to: to1@xxx.xx,to2@xxx.xx
      cc: cc@xxx.xx
      bcc: bcc@xxx.xx
      debug: false
```

### Wechat Enterprise Notification Strategy

Please refer to [here](/en/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler/#wechat-enterprise-notification-strategy) for more details.

Maven POM:
```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-wechat</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```
```yaml
elasticjob:
  regCenter:
    ...
  jobs:
    ...
    jobErrorHandlerType: WECHAT 
  props:
    wechat:
      webhook: you_webhook
      connectTimeout: 3000
      readTimeout: 5000
```

### Dingtalk Notification Strategy

Please refer to [here](/en/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler/#dingtalk-notification-strategy) for more details.

Maven POM:
```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-dingtalk</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```
```yaml
elasticjob:
  regCenter:
    ...
  jobs:
    ...
    jobErrorHandlerType: DINGTALK 
  props:
    dingtalk:
       webhook: you_webhook
       keyword: you_keyword
       secret: you_secret
       connectTimeout: 3000
       readTimeout: 5000
```
