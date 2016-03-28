+++
date = "2016-01-27T16:14:21+08:00"
title = "Release Notes"
weight=1
+++

# Release Notes

## 1.0.6-SNAPSHOT

### 功能提升

1. [ISSUE #71](https://github.com/dangdangdotcom/elastic-job/issues/71) 作业关闭功能（shutdown）
1. [ISSUE #72](https://github.com/dangdangdotcom/elastic-job/issues/72) 关闭的作业可删除

## 1.0.5

### 功能提升

1. [ISSUE #2](https://github.com/dangdangdotcom/elastic-job/issues/2) 增加前置和后置任务
1. [ISSUE #60](https://github.com/dangdangdotcom/elastic-job/issues/60) 可于DataFlow类型作业定制化线程池配置
1. [ISSUE #62](https://github.com/dangdangdotcom/elastic-job/issues/61) 作业状态清理提速
1. [ISSUE #65](https://github.com/dangdangdotcom/elastic-job/issues/65) 增加前置和后置任务Spring命名空间支持

### 缺陷修正

1. [ISSUE #61](https://github.com/dangdangdotcom/elastic-job/issues/61) 分片和主节点选举同时发生时，死锁问题解决
1. [ISSUE #63](https://github.com/dangdangdotcom/elastic-job/issues/63) 获取作业TreeCache时可能会获取到前缀相同的其他作业的TreeCache
1. [ISSUE #69](https://github.com/dangdangdotcom/elastic-job/issues/69) 分片时如在Zk中有的作业服务器sharding节点不存在将导致无法重新分片

### 结构调整

1. [ISSUE #59](https://github.com/dangdangdotcom/elastic-job/issues/59) 将elastic-job依赖的curator从`2.8.0`升级至`2.10.0`

## 1.0.4

### 功能提升
1. [ISSUE #16](https://github.com/dangdangdotcom/elastic-job/issues/16) 提供内嵌zookeeper，简化开发环境
1. [ISSUE #28](https://github.com/dangdangdotcom/elastic-job/issues/28) DataFlow类型作业增加`processData`批量处理数据的方法
1. [ISSUE #56](https://github.com/dangdangdotcom/elastic-job/issues/56) 作业自定义参数设置

### 结构调整

1. [ISSUE #57](https://github.com/dangdangdotcom/elastic-job/issues/57) 精简模块，移除`elastic-job-test`模块
1. [ISSUE #58](https://github.com/dangdangdotcom/elastic-job/issues/58) 增加批量处理功能导致的作业类型接口变更

## 1.0.3

### 功能提升

1. [ISSUE #39](https://github.com/dangdangdotcom/elastic-job/issues/39) 增加作业辅助监听功能，通过dump命令抓取作业运行时信息
1. [ISSUE #43](https://github.com/dangdangdotcom/elastic-job/issues/43) 增加作业异常处理回调接口

### 缺陷修正

1. [ISSUE #30](https://github.com/dangdangdotcom/elastic-job/issues/30) 注册中心宕机较长时间后重新恢复，作业无法继续执行
1. [ISSUE #36](https://github.com/dangdangdotcom/elastic-job/issues/36) 任务在控制台暂停之后，无法恢复运行
1. [ISSUE #40](https://github.com/dangdangdotcom/elastic-job/issues/40) TreeCache使用粒度过粗导致内存溢出

## 1.0.2

### 功能提升

1. [ISSUE #6](https://github.com/dangdangdotcom/elastic-job/issues/6) 校对作业服务器与注册中心时间误差
1. [ISSUE #8](https://github.com/dangdangdotcom/elastic-job/issues/8) 增加misfire开关，默认开启错过任务重新执行
1. [ISSUE #9](https://github.com/dangdangdotcom/elastic-job/issues/9) 分片策略可配置化
1. [ISSUE #10](https://github.com/dangdangdotcom/elastic-job/issues/10) 提供根据作业名称hash值取奇偶数分片排序策略
1. [ISSUE #14](https://github.com/dangdangdotcom/elastic-job/issues/14) 控制台修改cron表达式后，任务将实时更新cron
1. [ISSUE #20](https://github.com/dangdangdotcom/elastic-job/issues/20) 运维界面任务列表显示增加cron表达式
1. [ISSUE #54](https://github.com/dangdangdotcom/elastic-job/issues/54) SequencePerpetual类型作业性能提升，将抓取数据改为多线程，之前仅处理数据为多线程
1. [ISSUE #55](https://github.com/dangdangdotcom/elastic-job/issues/55) offset存储功能

### 缺陷修正

1. [ISSUE #1](https://github.com/dangdangdotcom/elastic-job/issues/1) 复杂网络环境下IP地址获取不准确的问题
1. [ISSUE #13](https://github.com/dangdangdotcom/elastic-job/issues/13) 作业抛出运行时异常后，后续不会继续触发
1. [ISSUE #53](https://github.com/dangdangdotcom/elastic-job/issues/53) Dataflow的Sequence类型作业采用多线程抓取数据

### 结构调整

1. [ISSUE #17](https://github.com/dangdangdotcom/elastic-job/issues/17) 作业类型接口变更，参见[1.0.2升级说明](http://dangdangdotcom.github.io/elastic-job/post/update_notes_1.0.2/)

## 1.0.1
1. 初始版本
