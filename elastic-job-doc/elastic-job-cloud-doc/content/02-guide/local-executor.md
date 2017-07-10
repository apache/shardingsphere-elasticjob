+++
toc = true
date = "2017-06-05T16:14:21+08:00"
title = "本地运行模式"
weight = 40
prev = "/02-guide/event-trace/"
next = "/03-design"
+++

在开发`Elastic-Job-Cloud`作业时，开发人员可以脱离`Mesos`环境，在本地运行和调试作业。可以利用本地运行模式充分的调试业务功能以及单元测试，完成之后再部署至`Mesos`集群。

本地运行作业无需安装`Mesos`环境。

## 配置

使用`com.dangdang.ddframe.job.cloud.executor.local.LocalCloudJobConfiguration`配置本地作业。

```java
LocalCloudJobConfiguration config = new LocalCloudJobConfiguration(
    new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("FooJob", "*/2 * * * * ?", 3) //1
        .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou")
        .jobParameter("dbName=dangdang").build(), "com.dangdang.foo.FooJob"), 
        1,                                                                               //2
        "testSimpleJob" , "applicationContext.xml");                                     //3
```

1. 配置作业类型和作业基本信息。
1. 配置当前运行的作业是第几个分片。
1. 配置Spring相关参数。

## 运行

使用`com.dangdang.ddframe.job.cloud.executor.local.LocalTaskExecutor`运行作业。

```java
new LocalTaskExecutor(localJobConfig).execute();
```
