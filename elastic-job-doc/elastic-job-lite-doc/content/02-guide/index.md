+++
icon = "<b>2. </b>"
date = "2016-12-12T16:06:17+08:00"
title = "使用指南"
weight = 0
prev = "/01-start/deploy-guide/"
next = "/02-guide/web-console/"
chapter = true
+++

# 本章导航
 
 - Elastic-Job提供了简单易用的[运维平台](/02-guide/web-console/)，方便用户操作及查询作业。
 
 - [配置手册](/02-guide/config-manual/)介绍了如何通过Java Code和Spring两种方式配置。
 
 - 一般情况Elastic-Job是通过平均分配算法的分片策略数据的，但也可以选择哈希及轮转等策略，或者自己定义[作业分片策略](/02-guide/job-sharding-strategy/)。
 
 - 为了便于记录、查询、统计及监控作业运行情况，Elastic-Job提供了[事件追踪](/02-guide/event-trace/)功能，也可自行对[作业运行状态监控](/02-guide/execution-monitor/)。
 
 - 由于无法在生产环境调试，通过[dump](/02-guide/dump/)可以把作业运行相关信息dump出来，方便开发者debug分析。
 
 - 最后，Elastic-Job还提供了其它扩展功能，如：[作业监听器](/02-guide/job-listener/)、[自诊断修复](/02-guide/job-reconcile/)、[定制化处理](/02-guide/customized-hook/)及[操作手册](/02-guide/operation-manual/)等。