# ElasticJob-Lite Spring Boot Starter

## Getting Started

### Add Dependencies

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-lite-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

Tracing starter is optional.
User can provide a bean of `TracingConfiguration` manually.
```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-tracing-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### Implement a ElasticJob

```java
@Component
public class SpringBootSimpleJob implements SimpleJob {

    @Autowired
    private FooRepository fooRepository;

    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println(String.format("Item: %s | Time: %s | Thread: %s | %s",
                shardingContext.getShardingItem(), new SimpleDateFormat("HH:mm:ss").format(new Date()), Thread.currentThread().getId(), "SIMPLE"));
        List<Foo> data = fooRepository.findTodoData(shardingContext.getShardingParameter(), 10);
        for (Foo each : data) {
            fooRepository.setCompleted(each.getId());
        }
    }
}
```

### Configure Registry Center, Jobs and Tracing Configuration

`application.yml`
```yaml
elasticjob:
  tracing:
    type: RDB
  regCenter:
    serverLists: localhost:6181
    namespace: elasticjob-lite-springboot
  jobs:
    classed:
      org.apache.shardingsphere.elasticjob.simple.job.SimpleJob:
        - jobName: simpleJob
          cron: 0/5 * * * * ?
          shardingTotalCount: 3
          shardingItemParameters: 0=Beijing,1=Shanghai,2=Guangzhou
      org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob:
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
            script.command.line: /home/sia/test/hello.sh
```

### Run it!

Log fragments after started:
```
Item: 2 | Time: 14:18:00 | Thread: 391 | DATAFLOW FETCH
Item: 0 | Time: 14:18:00 | Thread: 386 | DATAFLOW FETCH
Item: 0 | Time: 14:18:00 | Thread: 387 | SIMPLE
Item: 1 | Time: 14:18:00 | Thread: 389 | SIMPLE
Item: 2 | Time: 14:18:00 | Thread: 393 | SIMPLE
Item: 1 | Time: 14:18:00 | Thread: 388 | DATAFLOW FETCH
Item: 1 | Time: 14:18:00 | Thread: 388 | DATAFLOW PROCESS
Item: 0 | Time: 14:18:00 | Thread: 386 | DATAFLOW PROCESS
Item: 2 | Time: 14:18:00 | Thread: 391 | DATAFLOW PROCESS
SCRIPT Job: {"jobName":"scriptJob","taskId":"scriptJob@-@0,1,2@-@READY@-@192.168.3.233@-@28250","shardingTotalCount":3,"jobParameter":"","shardingItem":1}
SCRIPT Job: {"jobName":"scriptJob","taskId":"scriptJob@-@0,1,2@-@READY@-@192.168.3.233@-@28250","shardingTotalCount":3,"jobParameter":"","shardingItem":2}
SCRIPT Job: {"jobName":"scriptJob","taskId":"scriptJob@-@0,1,2@-@READY@-@192.168.3.233@-@28250","shardingTotalCount":3,"jobParameter":"","shardingItem":0}
```