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

| 属性名    | 说明        | 默认值                    |
| -------- |:----------- |:------------------------ |
| host     | TODO        | 无                       |
| port     | TODO        | 无                       |
| username | TODO        | 无                       |
| password | TODO        | 无                       |
| useSsl   | TODO        | 无                       |
| subject  | TODO        | ElasticJob error message |
| from     | TODO        | 无                       |
| to       | TODO        | 无                       |
| cc       | TODO        | 无                       |
| bcc      | TODO        | 无                       |
| debug    | TODO        | 无                       |

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

| 属性名                     | 说明        | 默认值    |
| ------------------------- |:----------- |:-------- |
| webhook                   | TODO        | 无        |
| connectTimeoutMillisecond | TODO        | 3000 毫秒 |
| readTimeoutMillisecond    | TODO        | 5000 毫秒 |

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

| 属性名                     | 说明        | 默认值    |
| ------------------------- |:----------- |:-------- |
| webhook                   | TODO        | 无        |
| keyword                   | TODO        | 无        |
| secret                    | TODO        | 无        |
| connectTimeoutMillisecond | TODO        | 3000 毫秒 |
| readTimeoutMillisecond    | TODO        | 5000 毫秒 |
