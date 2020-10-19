+++
title = "Use Spring Boot Starter"
weight = 2
chapter = true
+++

ElasticJob-Lite Spring Boot Starter has already integrated TracingConfiguration configuration.
What developers need to do is register a bean of DataSource into the Spring IoC Container and set the type of data source.
Then the Starter will create an instance of TracingConfiguration and register it into the container.

## Import Maven Dependency

Import spring-boot-starter-jdbc for DataSource register or create a bean of DataSource manually.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
    <version>${springboot.version}</version>
</dependency>
```

## Configuration

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

## Job Start

TracingConfiguration will be registered into the IoC container imperceptibly after setting tracing type to RDB.
If elasticjob-lite-spring-boot-starter was imported, developers need to do nothing else. 
The instances of JobBootstrap will use the TracingConfiguration automatically.
