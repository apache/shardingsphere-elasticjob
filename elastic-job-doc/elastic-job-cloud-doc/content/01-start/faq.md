+++
toc = true
date = "2016-01-27T16:14:21+08:00"
title = "FAQ"
weight = 2
prev = "/01-start/quick-start/"
next = "/01-start/dev-guide/"
+++

### 1. 阅读源码时为什么会出现编译错误?

回答：

Elastic-Job使用lombok实现极简代码。关于更多使用和安装细节，请参考[lombok官网](https://projectlombok.org/download.html)。

***

### 2. Elastic-Job-Cloud有何使用限制?

回答：

* 作业启动成功后修改作业名称视为新作业，原作业废弃。

***

### 3. 是否支持动态添加作业?

回答：

动态添加作业这个概念每个人理解不尽相同。

elastic-job-cloud为mesos框架，由mesos负责作业启动和分发。
但需要将作业打包上传，并调用elastic-job-cloud提供的REST API写入注册中心。
打包上传属于部署系统的范畴elastic-job-cloud并未涉及。

elastic-job已做了基本动态添加功能，但无法做到真正意义的完全自动化添加。

***

### 4. 使用Spring版本有何限制?

回答：

Elastic-Job的Spring版本支持从3.1.0.RELEASE至4的任何版本。Spring 5由于仅支持JDK 8及其以上版本，因此目前并不支持。Spring 3.1.0之前的版本对占位符的使用与目前不同，因此不再支持。Elastic-Job并未包含Spring的maven依赖，请自行添加您需要的版本。

***

### 5. Zookeeper版本不是3.4.6会有什么问题?

回答：

根据测试，使用3.3.6版本的Zookeeper在使用Curator 2.10.0的CuratorTransactionFinal的commit时会导致死锁。

***

### 6. Elastic-Job 2.0.5版本使用Cloud需要注意哪些问题?

回答：

对于Elastic Job Cloud，原作业维度配置无法满足易用性和扩展性等需求，因此在elastic-job 2.0.5Cloud版本中增加了作业APP的概念，即作业打包部署后的应用，描述了作业启动需要用到的CPU、内存、启动脚本及应用下载路径等基本信息，每个APP可以包含一个或多个作业。

**增加JOB APP API**

* 将作业打包部署后发布作业APP。

* 作业APP配置参数cpuCount,memoryMB分别代表应用启动时需要用到的CPU及内存。

**调整JOB API**

* 新增作业时，必须先发布打包部署后的作业APP。

* 作业配置参数cpuCount,memoryMB分别代表作业运行时需要用到的CPU及内存。
