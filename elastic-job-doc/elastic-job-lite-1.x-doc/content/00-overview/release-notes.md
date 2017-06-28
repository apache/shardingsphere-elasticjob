+++
toc = true
date = "2016-01-27T22:38:50+08:00"
title = "Release Note"
weight = 2
prev = "/00-overview/intro/"
next = "/00-overview/update-notes-1.1.0/"
+++

## 1.1.1

### 结构调整

1. [ISSUE #116](https://github.com/dangdangdotcom/elastic-job/issues/116) 作业接口的handleJobExecutionException参数变更

### 功能提升

1. [ISSUE #110](https://github.com/dangdangdotcom/elastic-job/issues/110) 手动触发作业

### 缺陷修正
1. [ISSUE #99](https://github.com/dangdangdotcom/elastic-job/issues/99) 删除作业异步导致作业删除后, 还未结束的作业继续创建zk数据

## 1.1.0

### 结构调整

1. [ISSUE #97](https://github.com/dangdangdotcom/elastic-job/issues/97) JobConfiguration重构为SimpleJobConfiguration，DataFlowJobConfiguration，ScriptJobConfiguration
1. [ISSUE #102](https://github.com/dangdangdotcom/elastic-job/issues/102) 重新定义Java/Spring Config API，使用Factory+Builder模式代替原有的Constructor+Setter模式
1. [ISSUE #104](https://github.com/dangdangdotcom/elastic-job/issues/104) 移除@Deprecated代码
1. [ISSUE #105](https://github.com/dangdangdotcom/elastic-job/issues/105) 重构Spring命名空间驼峰式定义
1. [ISSUE #106](https://github.com/dangdangdotcom/elastic-job/issues/106) isStreaming配置化
1. [ISSUE #107](https://github.com/dangdangdotcom/elastic-job/issues/107) reg-center更名为registry-center-ref

## 1.0.8

### 新功能

1. [ISSUE #95](https://github.com/dangdangdotcom/elastic-job/issues/95) 增加脚本类型作业支持

## 1.0.7

### 结构调整

1. [ISSUE #88](https://github.com/dangdangdotcom/elastic-job/issues/88) stop作业改名为pause

### 新功能

1. [ISSUE #91](https://github.com/dangdangdotcom/elastic-job/issues/91) 作业生命周期操作API

### 功能提升

1. [ISSUE #84](https://github.com/dangdangdotcom/elastic-job/issues/84) 控制台提供作业启用/禁用按钮操作
1. [ISSUE #87](https://github.com/dangdangdotcom/elastic-job/issues/87) 调整主节点选举流程，作业关闭，禁用和暂停将触发主节点选举
1. [ISSUE #93](https://github.com/dangdangdotcom/elastic-job/issues/93) 注册中心配置提供baseSleepTimeMilliseconds、maxSleepTimeMilliseconds和maxRetries的默认值

### 缺陷修正
1. [ISSUE #92](https://github.com/dangdangdotcom/elastic-job/issues/92) 修改分片总数参数导致仅单一节点执行的监听抛出超时异常

## 1.0.6

### 功能提升

1. [ISSUE #71](https://github.com/dangdangdotcom/elastic-job/issues/71) 作业关闭功能（shutdown）
1. [ISSUE #72](https://github.com/dangdangdotcom/elastic-job/issues/72) 关闭的作业可删除
1. [ISSUE #81](https://github.com/dangdangdotcom/elastic-job/issues/81) 使用集中清理作业上次结束状态代替各自清理，各自清理可能导致作业机下线而产生未清理的结束状态

### 缺陷修正

1. [ISSUE #74](https://github.com/dangdangdotcom/elastic-job/issues/74) 流式处理且失效转移时，失效转移的分片项不能执行一次即停止
1. [ISSUE #77](https://github.com/dangdangdotcom/elastic-job/issues/77) dataflow类型作业，fetchData如果有数据，则应与processData成对执行
1. [ISSUE #78](https://github.com/dangdangdotcom/elastic-job/issues/78) Spring方式配置作业监听启用AOP导致不能正常使用问题

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

1. [ISSUE #59](https://github.com/dangdangdotcom/elastic-job/issues/59) 将elastic-job依赖的curator从2.8.0升级至2.10.0

## 1.0.4

### 功能提升
1. [ISSUE #16](https://github.com/dangdangdotcom/elastic-job/issues/16) 提供内嵌zookeeper，简化开发环境
1. [ISSUE #28](https://github.com/dangdangdotcom/elastic-job/issues/28) DataFlow类型作业增加processData批量处理数据的方法
1. [ISSUE #56](https://github.com/dangdangdotcom/elastic-job/issues/56) 作业自定义参数设置

### 结构调整

1. [ISSUE #57](https://github.com/dangdangdotcom/elastic-job/issues/57) 精简模块，移除elastic-job-test模块
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

1. [ISSUE #17](https://github.com/dangdangdotcom/elastic-job/issues/17) 作业类型接口变更

## 1.0.1
1. 初始版本
