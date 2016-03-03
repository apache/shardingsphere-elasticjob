+++
date = "2016-01-27T16:14:21+08:00"
title = "Release Notes"
weight=1
+++

# Release Notes

## 1.0.5-SNAPSHOT
1. 调整作业执行节点完成状态清理策略，将原来的由主节点统一清理所有上次运行状态，改为由每次作业触发时清理自己所属分片的上次运行状态
1. 之前的DataFlowElasticJob由于使用newCachedThreadPool，可能会导致线程过多冲垮线程池。现在于DataFlowElasticJob接口增加getExecutorService方法，使用方可通过覆盖该方法自行提供ExecutorService，默认为newCachedThreadPool
1. 修正BUG，分片和主节点选举同时发生时，可能会锁死。通过修正分片中条件判断改善多线程环境下分片锁死的可能性
1. 修正BUG，将分配分片步骤和清理分片标记步骤放入同一事务，保持数据完整性，并防止分片成功后分片标记未清理导致的死锁可能性
1. 修正BUG，将清理分片步骤放入同一事务，保持数据完整性

## 1.0.4
1. 精简项目模块，移除elastic-job-test模块
1. 功能提升，作业自定义参数设置功能，可通过JobScheduler的setField方法完成
1. 功能提升Issuse#16，提供内嵌zookeeper，简化开发环境
1. 功能提升Issuse#28，DataFlow类型作业增加processData批量处理数据的方法
由于功能提升导致之前的数据流作业接口由2个变为4个，原AbstractThroughputDataFlowElasticJob和AbstractSequenceDataFlowElasticJob改变为AbstractIndividualThroughputDataFlowElasticJob、AbstractIndividualSequenceDataFlowElasticJob，并增加两种作业类型：AbstractBatchThroughputDataFlowElasticJob、AbstractBatchSequenceDataFlowElasticJob
1. 接口变更，便于分类清晰，将com.dangdang.ddframe.job.plugin.job.type.AbstractSimpleElasticJob移动至com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob，数据流作业类型移动至com.dangdang.ddframe.job.plugin.job.type.dataflow包

## 1.0.3
1. 修正Issuse#30，注册中心宕机较长时间后重新恢复，作业仍然无法继续执行
1. 修正Issuse#36，任务在控制台暂停之后，无法恢复运行
1. 功能提升Issuse#39，增加作业辅助监听功能，通过dump命令抓取作业运行时信息
1. 修正Issuse#40，TreeCache使用粒度过粗，导致out of memory
1. 功能提升Issuse#43，增加作业异常处理回调接口

## 1.0.2
1. 修正Issuse#1，改正复杂网络环境下IP地址获取不准确的问题
1. SequencePerpetual类型作业性能提升，将抓取数据改为多线程，之前仅处理数据为多线程
1. 修正Bug，不能在ZookeeperRegistryCenter中设置connectionTimeoutMilliseconds
1. 功能提升，增加offset存储功能，用于记录上次处理数据的位置，记录在Zookeeper中。
1. 功能提升Issuse#6，校对作业服务器与注册中心时间误差。
1. 功能提升Issuse#8，增加misfire开关，默认开启错过任务重新执行。
1. 功能提升Issuse#9，分片策略可配置化。
1. 功能提升Issuse#10，jobname的hash值取奇偶数分片排序策略。
1. 修正Issuse#13，job抛出运行时异常后，后续不会继续触发。
1. 功能提升Issuse#14，控制台修改cron表达式后，任务不会实时更新cron。
1. 作业类型接口变更，参见1.0.2升级说明。

## 1.0.1
1. 初始版本

