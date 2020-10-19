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

| 属性名    | 说明                 | 默认值                   |
| -------- |:-------------------- |:------------------------ |
| host     | 邮件服务主机地址      | 无                       |
| port     | 邮件服务主机端口      | 无                       |
| username | 用户名               | 无                       |
| password | 密码                 | 无                       |
| useSsl   | 是否启用ssl加密传输   | 无                       |
| subject  | 邮件主题             | ElasticJob error message |
| from     | 发送方邮箱地址       | 无                        |
| cc       | 抄送邮箱地址         | 无                        |
| bcc      | 密送邮箱地址         | 无                        |
| debug    | 是否开启debug        | 无                        |

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

| 属性名                     | 说明                                | 默认值    |
| ------------------------- |:----------------------------------- |:-------- |
| webhook                   | 企业微信机器人的webhook地址           | 无        |
| connectTimeoutMillisecond | 与企业微信服务器建立连接的超时时间       | 3000 毫秒 |
| readTimeoutMillisecond    | 从企业微信服务器读取到可用资源的超时时间  | 5000 毫秒 |

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

| 属性名                     | 说明                              | 默认值    |
| ------------------------- |:----------------------------------|:-------- |
| webhook                   | 钉钉机器人的webhook地址            | 无        |
| keyword                   | 自定义关键词                       | 无        |
| secret                    | 签名的密钥                         | 无        |
| connectTimeoutMillisecond | 与钉钉服务器建立连接的超时时间      | 3000 毫秒 |
| readTimeoutMillisecond    | 从钉钉服务器读取到可用资源的超时时间 | 5000 毫秒 |
