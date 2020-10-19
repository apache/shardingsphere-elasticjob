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

| Name           | Description                                      | Default Value            |
| -------------- |:------------------------------------------------ |:------------------------ |
| email.host     | Email service host address                       | None                     |
| email.port     | Email service port                               | None                     |
| email.username | Username                                         | None                     |
| email.password | Password                                         | None                     |
| email.useSsl   | Whether to enable ssl encrypted transmission     | None                     |
| email.subject  | Email Subject                                    | ElasticJob error message |
| email.from     | Sender email address                             | None                     |
| email.to       | Recipient's email address                        | None                     |
| email.cc       | Carbon copy email address                        | None                     |
| email.bcc      | Blind carbon copy email address                  | None                     |
| email.debug    | Whether to enable debug                          | None                     |

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

| Name                             | Description                                                                | Default Value     |
| -------------------------------- |:---------------------------------------------------------------------------|:----------------- |
| wechat.webhook                   | The webhook address of the wechat robot                                    | None              |
| wechat.connectTimeoutMillisecond | The timeout period for establishing a connection with the wechat server    | 3000 milliseconds |
| wechat.readTimeoutMillisecond    | The timeout period for reading available resources from the wechat server  | 5000 milliseconds |

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

| Name                               | Description                                                                 | Default Value     |
| ---------------------------------- |:--------------------------------------------------------------------------- |:------------------|
| dingtalk.webhook                   | The webhook address of the dingtalk robot                                   | None              |
| dingtalk.keyword                   | Custom keywords                                                             | None              |
| dingtalk.secret                    | Secret for dingtalk robot                                                   | None              |
| dingtalk.connectTimeoutMillisecond | The timeout period for establishing a connection with the dingtalk server   | 3000 milliseconds |
| dingtalk.readTimeoutMillisecond    | The timeout period for reading available resources from the dingtalk server | 5000 milliseconds |
