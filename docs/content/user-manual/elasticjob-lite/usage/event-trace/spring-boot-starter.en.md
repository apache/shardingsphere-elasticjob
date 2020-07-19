+++
title = "Use Spring Boot Starter"
weight = 2
chapter = true
+++

ElasticJob-Lite provides a Spring Boot Starter to configure TracingConfiguration automatically.
What developers need to do is register a bean of DataSource into the Spring IoC Container.
Then the Starter will create an instance of TracingConfiguration and register it into the container.

## Introduce elasticjob-tracing-spring-boot-starter dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-tracing-spring-boot-starter</artifactId>
    <version>${elasticjob.latest.version}</version>
</dependency>
```

## Register DataSource via spring-boot-starter-jdbc

Introduce dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
    <version>${springboot.version}</version>
</dependency>
```

Configure DataSource:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:job_event_storage
    driver-class-name: org.h2.Driver
    username: sa
    password:
```

## Usage

TracingConfiguration will be registered into the IoC container imperceptibly.
If elasticjob-lite-spring-boot-starter was introduced, developers need to do nothing else. 
The instances of JobBootstrap will use the TracingConfiguration automatically.
