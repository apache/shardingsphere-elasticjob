+++
title = "开发指南"
weight = 1
chapter = true
+++

## 作业开发

ElasticJob-Lite 和 ElasticJob-Cloud 提供统一作业接口，开发者仅需对业务作业进行一次开发，之后可根据不同的配置以及部署至不同环境。

作业开发详情请参见 [ElasticJob-Lite 使用手册](/cn/user-manual/elasticjob-lite/usage/)。

## 作业启动

需定义 `main` 方法并调用 `JobBootstrap.execute()`，例子如下：

```java
public class MyJobDemo {
    
    public static void main(final String[] args) {
        JobBootstrap.execute(new MyJob());
    }
}
```
