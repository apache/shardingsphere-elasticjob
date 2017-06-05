+++
toc = true
date = "2017-06-05T16:14:21+08:00"
title = "本地运行作业"
weight = 40
prev = "/02-guide/event-trace/"
next = "/03-design"
+++

在开发`Elastic-Job-Cloud`作业的时候，开发人员会希望能够本地运行作业。目前作业云提供了该功能，您只需要使用简单的API配置作业，就可以像在
`Mesos`集群中一样本地运行作业。

本地运行作业无需安装`Mesos`环境。

## 本地作业配置

使用`com.dangdang.ddframe.job.cloud.executor.local.LocalCloudJobConfiguration`来配置本地作业。

```java
LocalCloudJobConfiguration config = new LocalCloudJobConfiguration(
        new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("FooJob", "*/2 * * * * ?", 3) //1
                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou")
                .jobParameter("dbName=dangdang").build()
                , "com.dangdang.foo.FooJob")
                , LocalCloudJobExecutionType.DAEMON   //2
                , 1                                   //3
                , "testSimpleJob"                     //4
                , "applicationContext.xml")
```

1. 配置作业类型和作业基本信息。
1. 配置作业的执行类型。
1. 配置当前运行的作业是第几个分片。
1. 配置Spring相关参数。

## 运行本地作业

使用`com.dangdang.ddframe.job.cloud.executor.local.LocalTaskExecutor`运行作业。

```java
new LocalTaskExecutor(config).execute();
```