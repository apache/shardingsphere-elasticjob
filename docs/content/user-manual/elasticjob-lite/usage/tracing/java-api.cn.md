+++
title = "使用 Java API"
weight = 1
chapter = true
+++

ElasticJob-Lite 在配置中提供了 TracingConfiguration，目前支持数据库方式配置。
开发者也可以通过 SPI 自行扩展。

```java
    // 初始化数据源
    DataSource dataSource = ...;
    // 定义日志数据库事件溯源配置
    TracingConfiguration tracingConfig = new TracingConfiguration<>("RDB", dataSource);
    // 初始化注册中心
    CoordinatorRegistryCenter regCenter = ...;
    // 初始化作业配置
    JobConfiguration jobConfig = ...;
    jobConfig.getExtraConfigurations().add(tracingConfig);
    new ScheduleJobBootstrap(regCenter, jobConfig).schedule();
```
