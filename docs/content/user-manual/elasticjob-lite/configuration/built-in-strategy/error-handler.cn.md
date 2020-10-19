+++
title = "错误处理策略"
weight = 3
+++

## 记录日志策略

类型：LOG

默认内置：是

记录作业异常日志，但不中断作业执行。

## 抛出异常策略

类型：THROW

默认内置：是

抛出系统异常并中断作业执行。

## 忽略异常策略

类型：IGNORE

默认内置：是

忽略系统异常且不中断作业执行。

## 邮件通知策略

类型：EMAIL

默认内置：否

发送邮件消息通知，但不中断作业执行。

Maven 坐标：

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-email</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

可配置属性：

| 属性名          | 说明        | 默认值                    |
| -------------- |:----------- |:------------------------ |
| email.host     | TODO        | 无                       |
| email.port     | TODO        | 无                       |
| email.username | TODO        | 无                       |
| email.password | TODO        | 无                       |
| email.useSsl   | TODO        | true                     |
| email.subject  | TODO        | ElasticJob error message |
| email.from     | TODO        | 无                       |
| email.to       | TODO        | 无                       |
| email.cc       | TODO        | 无                       |
| email.bcc      | TODO        | 无                       |
| email.debug    | TODO        | false                    |

## 企业微信通知策略

类型：WECHAT

默认内置：否

发送企业微信消息通知，但不中断作业执行。

Maven 坐标：

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-wechat</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

可配置属性：

| 属性名                            | 说明        | 默认值    |
| -------------------------------- |:----------- |:-------- |
| wechat.webhook                   | TODO        | 无        |
| wechat.connectTimeoutMillisecond | TODO        | 3000 毫秒 |
| wechat.readTimeoutMillisecond    | TODO        | 5000 毫秒 |

## 钉钉通知策略

类型：DINGTALK

默认内置：否

发送钉钉消息通知，但不中断作业执行。

Maven 坐标：

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-error-handler-dingtalk</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

可配置属性：

| 属性名                              | 说明        | 默认值    |
| ---------------------------------- |:----------- |:-------- |
| dingtalk.webhook                   | TODO        | 无        |
| dingtalk.keyword                   | TODO        | 无        |
| dingtalk.secret                    | TODO        | 无        |
| dingtalk.connectTimeoutMillisecond | TODO        | 3000 毫秒 |
| dingtalk.readTimeoutMillisecond    | TODO        | 5000 毫秒 |
