+++
title = "使用 Spring Boot Starter"
weight = 2
chapter = true
+++

ElasticJob-Lite 提供了自动配置 TracingConfiguration 的 Tracing Spring Boot Starter，
开发者只需注册一个 DataSource 到 Spring 容器中，
Starter 就会自动创建一个 TracingConfiguration 并注册到 Spring 容器中。

## 引入 elasticjob-tracing-spring-boot-starter

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-tracing-spring-boot-starter</artifactId>
    <version>${elasticjob.latest.version}</version>
</dependency>
```

## 借助 spring-boot-starter-jdbc 注册 DataSource

引入依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
    <version>${springboot.version}</version>
</dependency>
```

配置 DataSource：
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:job_event_storage
    driver-class-name: org.h2.Driver
    username: sa
    password:
```

## 使用

TracingConfiguration 会自动注册到容器中，如果与 elasticjob-lite-spring-boot-starter 配合使用，
开发者无需进行其他额外的操作，作业启动器会自动使用创建的 TracingConfiguration。
