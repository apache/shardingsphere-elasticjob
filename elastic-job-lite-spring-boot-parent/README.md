# ElasticJobSpringBootStarter

## Getting started


### 1. Import elastic-job-lite-spring-boot-starter

Maven:
```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elastic-job-lite-spring-boot-starter</artifactId>
    <version>${elasticjob.version}</version>
</dependency>
```

### 2. Configure RegistryCenter and Tracing(Optional)

```yaml
elasticjob:
  tracing:
    type: RDB
  registryCenter:
    zookeeper:
      serverLists: localhost:6181
      namespace: spring_boot_demo

spring:
  datasource:
    url: jdbc:h2:mem:job_event_storage
    driver-class-name: org.h2.Driver
    username: sa
    password:
```

### 3. Create ElasticJob

```java
@ElasticJob(
        jobBootstrapType = JobBootstrapType.SCHEDULE,
        jobName = "beanSimpleJob",
        shardingTotalCount = 3,
        shardingItemParameters = "0=Beijing,1=Shanghai,2=Guangzhou",
        cron = "0/10 * * * * ?"
)
@Tracing
@RegistryCenter
public class SpringBootSimpleJob implements SimpleJob {

    @Autowired
    private FooRepository fooRepository;

    @Override
    public void execute(ShardingContext shardingContext) {
        // Do something
    }
}
```

### 4. Run your SpringBoot application!

## Annotations

### @RegistryCenter

This annotation is optional. Set value to use the specific RegistryCenter.
 Otherwise it will lookup RegistryCenter by type.
 
### @Tracing

This annotation is optional. If absent, no tracingConfiguration will be used.
Set value to use the specific RegistryCenter. Otherwise it will lookup RegistryCenter by type.
