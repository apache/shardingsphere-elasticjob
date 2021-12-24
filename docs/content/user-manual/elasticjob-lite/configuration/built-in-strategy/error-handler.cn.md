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

| 属性名          | 说明                | 是否必填  | 默认值                    |
| -------------- |:------------------- |:-------- |:------------------------ |
| email.host     | 邮件服务器地址        | 是       | -                        |
| email.port     | 邮件服务器端口        | 是       | -                        |
| email.username | 邮件服务器用户名      | 是       | -                        |
| email.password | 邮件服务器密码        | 是       | -                        |
| email.useSsl   | 是否启用 SSL 加密传输 | 否       | true                     |
| email.subject  | 邮件主题             | 否       | ElasticJob error message |
| email.from     | 发送方邮箱地址        | 是       | -                        |
| email.to       | 接收方邮箱地址        | 是       | -                        |
| email.cc       | 抄送邮箱地址          | 否       | null                     |
| email.bcc      | 密送邮箱地址          | 否       | null                     |
| email.debug    | 是否开启调试模式      | 否       | false                    |

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

| 属性名                             | 说明                                | 是否必填    | 默认值    |
| --------------------------------- |:----------------------------------- |:--------- |:--------- |
| wechat.webhook                    | 企业微信机器人的 webhook 地址         | 是         | -         |
| wechat.connectTimeoutMilliseconds | 与企业微信服务器建立连接的超时时间      | 否         | 3000 毫秒 |
| wechat.readTimeoutMilliseconds    | 从企业微信服务器读取到可用资源的超时时间 | 否         | 5000 毫秒 |

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

| 属性名                               | 说明                              | 是否必填 | 默认值    |
| ----------------------------------- |:----------------------------------|:------- |:-------- |
| dingtalk.webhook                    | 钉钉机器人的 webhook 地址           | 是       | -       |
| dingtalk.keyword                    | 自定义关键词                       | 否       | null     |
| dingtalk.secret                     | 签名的密钥                         | 否       | null     |
| dingtalk.connectTimeoutMilliseconds | 与钉钉服务器建立连接的超时时间       | 否       | 3000 毫秒 |
| dingtalk.readTimeoutMilliseconds    | 从钉钉服务器读取到可用资源的超时时间  | 否       | 5000 毫秒 |
