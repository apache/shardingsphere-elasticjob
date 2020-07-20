+++
title = "使用 Spring Boot Starter"
weight = 3
chapter = true
+++

ElasticJob-Lite 提供自定义的 Spring Boot Starter，可以与 Spring Boot 配合使用。
基于 ElasticJob Spring Boot Starter 使用 ElasticJob ，用户无需手动创建 CoordinatorRegistryCenter、JobBootstrap 等实例，
只需实现核心作业逻辑并辅以少量配置，即可利用轻量、无中心化的 ElasticJob 解决分布式调度问题。

## 作业配置

### 实现作业逻辑

作业逻辑实现与 ElasticJob 的其他使用方式并没有较大的区别，只需将当前作业注册为 Spring 容器中的 bean。

**线程安全问题**

Bean 默认是单例的，如果该作业实现会在同一个进程内被创建出多个 `JobBootstrap` 的实例，
可以考虑设置 Scope 为 `prototype`。

```java
@Component
public class SpringBootDataflowJob implements DataflowJob<Foo> {
    
    @Override
    public List<Foo> fetchData(final ShardingContext shardingContext) {
        // 获取数据
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<Foo> data) {
        // 处理数据
    }
}
```

### 配置协调服务与作业

在配置文件中指定 ElasticJob 所使用的 Zookeeper。配置前缀为 `elasticjob.reg-center`。

实现了 ElasticJob 的作业逻辑属于 classed 类型作业，需要配置在 `elasticjob.jobs.classed` 下，
`elasticjob.jobs.classed` 是一个 Map，限定类名作为 key，value 是一个 List<JobConfigurationPOJO>，
Starter 会根据该配置自动创建 `OneOffJobBootstrap` 或 `ScheduleJobBootstrap` 的实例并注册到 Spring 容器中。

配置参考：

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

## 作业启动

### 定时调度

定时调度作业在 Spring Boot 应用程序启动完成后会自动启动，无需其他额外操作。

### 一次性调度

一次性调度的作业的执行权在开发者手中，开发者可以在需要调用作业的位置注入 `OneOffJobBootstrap`，
通过 `execute()` 方法执行作业。

**关于@DependsOn注解**

JobBootstrap 由 Starter 动态创建，如果依赖方的实例化时间早于 Starter 创建 JobBootstrap，将无法注入 JobBoostrap 的实例。

也可以通过 ApplicationContext 获取 JobBootstrap 的 Bean。

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

