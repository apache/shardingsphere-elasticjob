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
 
 - 一般情况Elastic-Job是通过平均分配算法的分片策略数据的，但也可以选择哈希及轮转等策略，或者自己定义[作业分片策略](/02-guide/job-sharding-strategy/)。
 
 - 由于无法在生产环境调试，通过[dump](/02-guide/dump/)可以把作业运行相关信息dump出来，方便开发者debug分析。
 
 - 为了便于监控作业运行情况，Elastic-Job提供[作业运行状态监控](/02-guide/execution-monitor/)功能。