+++
title = "Use Java API"
weight = 1
chapter = true
+++

ElasticJob-Lite currently provides `TracingConfiguration` based on database in the configuration.
Developers can also extend it through SPI.

```java
    // init DataSource
    DataSource dataSource = ...;
    // define tracing configuration based on relation database
    TracingConfiguration tracingConfig = new TracingConfiguration<>("RDB", dataSource);
    // init registry center
    CoordinatorRegistryCenter regCenter = ...;
    // init job configuration
    JobConfiguration jobConfig = ...;
jobConfig.getExtraConfigurations().add(tracingConfig);
    new ScheduleJobBootstrap(regCenter, jobConfig).schedule();
```
