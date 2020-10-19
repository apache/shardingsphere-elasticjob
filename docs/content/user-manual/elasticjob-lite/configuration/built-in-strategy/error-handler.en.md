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

| Name      | Description                                      | Default Value            |
| -------- |:------------------------------------------------ |:------------------------ |
| host     | Email service host address                       | None                     |
| port     | Email service port                               | None                     |
| username | Username                                         | None                     |
| password | Password                                         | None                     |
| useSsl   | Whether to enable ssl encrypted transmission     | true                     |
| subject  | Email Subject                                    | ElasticJob error message |
| from     | Sender email address                             | None                     |
| to       | Recipient's email address                        | None                     |
| cc       | Carbon copy email address                        | None                     |
| bcc      | Blind carbon copy email address                  | None                     |
| debug    | Whether to enable debug                          | false                     |

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

| Name                       | Description                                                                | Default Value     |
| ------------------------- |:---------------------------------------------------------------------------|:----------------- |
| webhook                   | The webhook address of the wechat robot                                    | None              |
| connectTimeoutMillisecond | The timeout period for establishing a connection with the wechat server    | 3000 milliseconds |
| readTimeoutMillisecond    | The timeout period for reading available resources from the wechat server  | 5000 milliseconds |

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

| Name                      | Description                                                                 | Default Value     |
| ------------------------- |:--------------------------------------------------------------------------- |:------------------|
| webhook                   | The webhook address of the dingtalk robot                                   | None              |
| keyword                   | Custom keywords                                                             | None              |
| secret                    | Secret for dingtalk robot                                                   | None              |
| connectTimeoutMillisecond | The timeout period for establishing a connection with the dingtalk server   | 3000 milliseconds |
| readTimeoutMillisecond    | The timeout period for reading available resources from the dingtalk server | 5000 milliseconds |
