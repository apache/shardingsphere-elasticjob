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

| Name           | Description | Default Value            |
| -------------- |:----------- |:------------------------ |
| email.host     | TODO        | None                     |
| email.port     | TODO        | None                     |
| email.username | TODO        | None                     |
| email.password | TODO        | None                     |
| email.useSsl   | TODO        | None                     |
| email.subject  | TODO        | ElasticJob error message |
| email.from     | TODO        | None                     |
| email.to       | TODO        | None                     |
| email.cc       | TODO        | None                     |
| email.bcc      | TODO        | None                     |
| email.debug    | TODO        | None                     |

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

| Name                             | Description | Default Value     |
| -------------------------------- |:----------- |:----------------- |
| wechat.webhook                   | TODO        | None              |
| wechat.connectTimeoutMillisecond | TODO        | 3000 milliseconds |
| wechat.readTimeoutMillisecond    | TODO        | 5000 milliseconds |

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

| Name                               | Description | Default Value     |
| ---------------------------------- |:----------- |:----------------- |
| dingtalk.webhook                   | TODO        | None              |
| dingtalk.keyword                   | TODO        | None              |
| dingtalk.secret                    | TODO        | None              |
| dingtalk.connectTimeoutMillisecond | TODO        | 3000 milliseconds |
| dingtalk.readTimeoutMillisecond    | TODO        | 5000 milliseconds |
