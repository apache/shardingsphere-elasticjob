+++
title = "使用 Spring Boot Starter"
weight = 2
chapter = true
+++

ElasticJob-Lite 的 Spring Boot Starter 集成了 TracingConfiguration 自动配置，
开发者只需注册一个 DataSource 到 Spring 容器中并在配置文件指定事件追踪数据源类型，
Starter 就会自动创建一个 TracingConfiguration 实例并注册到 Spring 容器中。

## 引入 Maven 依赖

引入 spring-boot-starter-jdbc 注册数据源或自行创建一个 DataSource Bean。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
    <version>${springboot.version}</version>
</dependency>
```

## 配置

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:job_event_storage
    driver-class-name: org.h2.Driver
    username: sa
    password:

elasticjob:
  tracing:
    type: RDB
```

## 作业启动

指定事件追踪数据源类型为 RDB，TracingConfiguration 会自动注册到容器中，如果与 elasticjob-lite-spring-boot-starter 配合使用，
开发者无需进行其他额外的操作，作业启动器会自动使用创建的 TracingConfiguration。
