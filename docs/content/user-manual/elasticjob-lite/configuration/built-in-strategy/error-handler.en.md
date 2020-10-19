+++
title = "Error Handler Strategy"
weight = 3
+++

## Log Strategy

Type: LOG

Built-in: Yes

Log error and do not interrupt job.

## Throw Strategy

Type: THROW

Built-in: Yes

Throw system exception and interrupt job.

## Ignore Strategy

Type: IGNORE

Built-in: Yes

Ignore exception and do not interrupt job.

## Email Notification Strategy

Type: EMAIL

Built-in: No

Send email message notification and do not interrupt job.

Maven POM: 

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-email</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

Configuration: 

| Name     | Description |
| -------- |:----------- |
| host     | TODO        |
| port     | TODO        |
| username | TODO        |
| password | TODO        |
| useSsl   | TODO        |
| subject  | TODO        |
| from     | TODO        |
| to       | TODO        |
| cc       | TODO        |
| bcc      | TODO        |
| debug    | TODO        |

## Wechat Enterprise Notification Strategy

Type: WECHAT

Built-in: No

Send wechat message notification and do not interrupt job

Maven POM: 

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-wechat</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

Configuration: 

| Name                      | Description |
| ------------------------- |:----------- |
| webhook                   | TODO        |
| connectTimeoutMillisecond | TODO        |
| readTimeoutMillisecond    | TODO        |

## Dingtalk Notification Strategy

Type: DINGTALK

Built-in: No

Send dingtalk message notification and do not interrupt job

Maven POM: 

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-dingtalk</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

Configuration: 

| Name                      | Description |
| ------------------------- |:----------- |
| webhook                   | TODO        |
| keyword                   | TODO        |
| secret                    | TODO        |
| connectTimeoutMillisecond | TODO        |
| readTimeoutMillisecond    | TODO        |
