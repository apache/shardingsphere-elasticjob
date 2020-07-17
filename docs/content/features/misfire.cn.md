+++
pre = "<b>3.5. </b>"
title = "错过任务重执行"
weight = 5
chapter = true
+++

ElasticJob 不允许作业在同一时间内叠加执行。
当作业的执行时长超过其运行间隔，错过任务重执行能够保证作业在完成上次的任务后继续执行逾期的作业。

## 概念

错过任务重执行功能可以使逾期未执行的作业在之前作业执行完成之后立即执行。
举例说明，若作业以每小时为间隔执行，每次执行耗时 30 分钟。如下如图所示。

![定时作业](https://shardingsphere.apache.org/elasticjob/current/img/misfire/job.png)

图中表示作业分别于 12:00，13:00 和 14:00 执行。图中显示的当前时间点为 13:00 的作业执行中。

如果 12：00 开始执行的作业在 13:10 才执行完毕，那么本该由 13:00 触发的作业则错过了触发时间，需要等待至 14:00 的下次作业触发。
如下如图所示。

![错过作业](https://shardingsphere.apache.org/elasticjob/current/img/misfire/job-missed.png)

在开启错过任务重执行功能之后，ElasticJob 将会在上次作业执行完毕后，立刻触发执行错过的作业。如下图所示。

![错过作业重执行](https://shardingsphere.apache.org/elasticjob/current/img/misfire/job-misfire.png)

在 13：00 和 14:00 之间错过的作业将会重新执行。 

## 适用场景

在一次运行耗时较长且间隔较长的作业场景，错过任务重执行是提升作业运行实时性的有效手段；
对于未见得关注单次作业的实时性的短间隔的作业来说，开启错过任务重执行并无必要。
