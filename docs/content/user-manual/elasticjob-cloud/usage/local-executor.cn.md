+++
title = "本地运行模式"
weight = 2
chapter = true
+++

在开发 ElasticJob-Cloud 作业时，开发人员可以脱离 Mesos 环境，在本地运行和调试作业。
可以利用本地运行模式充分的调试业务功能以及单元测试，完成之后再部署至 Mesos 集群。

本地运行作业无需安装 Mesos 环境。

```java
// 创建作业配置
JobConfiguration jobConfig = JobConfiguration.newBuilder("myJob", 3).cron("0/5 * * * * ?").build();

// 配置当前运行的作业的分片项
int shardingItem = 0;

// 创建本地执行器
new LocalTaskExecutor(new MyJob(), jobConfig, shardingItem).execute();
```
