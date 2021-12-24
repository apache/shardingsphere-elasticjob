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

`elasticjob.jobs` 是一个 Map，key 为作业名称，value 为作业类型与配置。
Starter 会根据该配置自动创建 `OneOffJobBootstrap` 或 `ScheduleJobBootstrap` 的实例并注册到 Spring 容器中。

配置参考：

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

## 作业启动

### 定时调度

定时调度作业在 Spring Boot 应用程序启动完成后会自动启动，无需其他额外操作。

### 一次性调度

一次性调度的作业的执行权在开发者手中，开发者可以在需要调用作业的位置注入 `OneOffJobBootstrap`，
通过 `execute()` 方法执行作业。

`OneOffJobBootstrap` bean 的名称通过属性 jobBootstrapBeanName 配置，注入时需要指定依赖的 bean 名称。
具体配置请参考[配置文档](/cn/user-manual/elasticjob-lite/configuration/spring-boot-starter)。

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

    // 通过 "@Resource" 注入
    @Resource(name = "myOneOffJobBean")
    private OneOffJobBootstrap myOneOffJob;
    
    @GetMapping("/execute")
    public String executeOneOffJob() {
        myOneOffJob.execute();
        return "{\"msg\":\"OK\"}";
    }

    // 通过 "@Autowired" 注入
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


## 配置错误处理策略

使用 ElasticJob-Lite 过程中当作业发生异常后，可采用以下错误处理策略。

| *错误处理策略名称*         | *说明*                            |  *是否内置* | *是否默认*| *是否需要额外配置* |
| ----------------------- | --------------------------------- |  -------  |  --------|  -------------  |
| 记录日志策略              | 记录作业异常日志，但不中断作业执行     |   是       |     是   |                 |
| 抛出异常策略              | 抛出系统异常并中断作业执行            |   是       |         |                 |
| 忽略异常策略              | 忽略系统异常且不中断作业执行          |   是       |          |                 |
| 邮件通知策略              | 发送邮件消息通知，但不中断作业执行     |            |          |      是         |
| 企业微信通知策略           | 发送企业微信消息通知，但不中断作业执行 |            |          |      是          |
| 钉钉通知策略              | 发送钉钉消息通知，但不中断作业执行     |            |          |      是          |

### 记录日志策略
```yaml
elasticjob:
  regCenter:
    ...
  jobs:
    ...
    jobErrorHandlerType: LOG 
```

### 抛出异常策略
```yaml
elasticjob:
  regCenter:
    ...
  jobs:
    ...
    jobErrorHandlerType: THROW 
```


### 忽略异常策略
```yaml
elasticjob:
  regCenter:
    ...
  jobs:
    ...
    jobErrorHandlerType: IGNORE 
```

### 邮件通知策略

请参考 [这里](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler/#邮件通知策略) 了解更多。

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

### 企业微信通知策略

请参考 [这里](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler/#企业微信通知策略) 了解更多。

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


### 钉钉通知策略

请参考 [这里](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler/#钉钉通知策略) 了解更多。

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
