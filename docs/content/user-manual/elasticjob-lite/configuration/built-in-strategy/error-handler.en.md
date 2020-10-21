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

| Name           | Description                                  | Required | Default Value            |
| -------------- |:-------------------------------------------- |:-------- |:------------------------ |
| email.host     | Email server host address                    | Yes      | -                        |
| email.port     | Email server port                            | Yes      | -                        |
| email.username | Email server username                        | Yes      | -                        |
| email.password | Email server password                        | Yes      | -                        |
| email.useSsl   | Whether to enable SSL encrypted transmission | No       | true                     |
| email.subject  | Email Subject                                | No       | ElasticJob error message |
| email.from     | Sender email address                         | Yes      | -                        |
| email.to       | Recipient's email address                    | Yes      | -                        |
| email.cc       | Carbon copy email address                    | No       | null                     |
| email.bcc      | Blind carbon copy email address              | No       | null                     |
| email.debug    | Whether to enable debug mode                 | No       | false                    |

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

| Name                              | Description                                                               | Required | Default Value     |
| --------------------------------- |:------------------------------------------------------------------------- |:-------- |:----------------- |
| wechat.webhook                    | The webhook address of the wechat robot                                   | Yes      | -                 |
| wechat.connectTimeoutMilliseconds | The timeout period for establishing a connection with the wechat server   | No       | 3000 milliseconds |
| wechat.readTimeoutMilliseconds    | The timeout period for reading available resources from the wechat server | No       | 5000 milliseconds |

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

| Name                                | Description                                                                 | Required | Default Value     |
| ----------------------------------- |:--------------------------------------------------------------------------- |:-------- |:----------------- |
| dingtalk.webhook                    | The webhook address of the dingtalk robot                                   | Yes      | -                 |
| dingtalk.keyword                    | Custom keywords                                                             | No       | null              |
| dingtalk.secret                     | Secret for dingtalk robot                                                   | No       | null              |
| dingtalk.connectTimeoutMilliseconds | The timeout period for establishing a connection with the dingtalk server   | No       | 3000 milliseconds |
| dingtalk.readTimeoutMilliseconds    | The timeout period for reading available resources from the dingtalk server | No       | 5000 milliseconds |
