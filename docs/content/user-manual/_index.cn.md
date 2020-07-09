+++
pre = "<b>4. </b>"
title = "用户手册"
weight = 4
chapter = true
+++

# 本章导航
 
 - ElasticJob 提供了简单易用的[运维平台](/cn/user-manual/web-console/)，方便用户操作及查询作业。
 
 - [配置手册](/cn/user-manual/job-config/)介绍了如何通过 Java 和 Spring 两种方式配置。
 
 - 一般情况 ElasticJob 是通过平均分配算法的分片策略数据的，但也可以选择哈希及轮转等策略，或者自己定义[作业分片策略](/cn/user-manual/job-sharding-strategy/)。
 
 - 为了便于记录、查询、统计及监控作业运行情况，ElasticJob 提供了[事件追踪](/cn/user-manual/event-trace/)功能，也可自行对[作业运行状态监控](/cn/user-manual/execution-monitor/)。
 
 - 由于无法在生产环境调试，通过[dump](/cn/user-manual/dump/)可以把作业运行相关信息导出，方便开发者调试分析。
 
 - 最后，ElasticJob 还提供了其它扩展功能，如：[作业监听器](/cn/user-manual/job-listener/)、[自诊断修复](/cn/user-manual/job-reconcile/)、[定制化处理](/cn/user-manual/customized-hook/)及[操作手册](/cn/user-manual/operation-manual/)等。
