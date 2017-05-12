## 2.1.2

### 新功能

1. [ISSUE #301](https://github.com/dangdangdotcom/elastic-job/issues/301) Console增加Guest权限配置，guest只允许查看，不允许更改
1. [ISSUE #312](https://github.com/dangdangdotcom/elastic-job/issues/312) Cloud支持自修复功能

### 功能提升

1. [ISSUE #293](https://github.com/dangdangdotcom/elastic-job/issues/293) Lite Console数据源配置增加连接测试功能
1. [ISSUE #296](https://github.com/dangdangdotcom/elastic-job/issues/296) Cloud运维界面重构，与lite风格一致
1. [ISSUE #302](https://github.com/dangdangdotcom/elastic-job/issues/302) 失效转移与作业运行状态监听分离
1. [ISSUE #304](https://github.com/dangdangdotcom/elastic-job/issues/304) Cloud增加与Mesos角色关联功能
1. [ISSUE #316](https://github.com/dangdangdotcom/elastic-job/issues/316) Lite中运行中任务关联进程ID

### 缺陷修正

1. [ISSUE #291](https://github.com/dangdangdotcom/elastic-job/issues/291) elastic-job控制台信息失败原因信息展示不全
1. [ISSUE #306](https://github.com/dangdangdotcom/elastic-job/issues/306) 切换是否监控作业执行状态且作业间隔时间短时可能发生作业无法继续运行
1. [ISSUE #310](https://github.com/dangdangdotcom/elastic-job/issues/310) 配置检查本机与注册中心的时间误差秒后，创建过多顺序节点

## 2.1.1

### 新功能

1. [ISSUE #242](https://github.com/dangdangdotcom/elastic-job/issues/242) Elastic-Job-Cloud支持删除应用及作业功能
1. [ISSUE #243](https://github.com/dangdangdotcom/elastic-job/issues/243) Elastic-Job-Cloud支持禁用/启用应用及作业功能

### 功能提升

1. [ISSUE #268](https://github.com/dangdangdotcom/elastic-job/issues/268) 精简POM依赖

### 缺陷修正

1. [ISSUE #266](https://github.com/dangdangdotcom/elastic-job/issues/266) Elastic-Job-Lite启动脚本指定端口无效
1. [ISSUE #269](https://github.com/dangdangdotcom/elastic-job/issues/269) EventTrace失败记录不受采样率影响并且记录失败时间
1. [ISSUE #270](https://github.com/dangdangdotcom/elastic-job/issues/270) 控制台点击按钮后发起两次请求
1. [ISSUE #272](https://github.com/dangdangdotcom/elastic-job/issues/272) Elastic-Job-Lite界面作业维度，只有全部服务器被禁用时，才应显示为被禁用
1. [ISSUE #275](https://github.com/dangdangdotcom/elastic-job/issues/275) 停掉Zookeeper后，再重启Zookeeper，任务不会继续执行
1. [ISSUE #276](https://github.com/dangdangdotcom/elastic-job/issues/276) 开启失效转移且分片任务执行后，任务会重复执行
1. [ISSUE #279](https://github.com/dangdangdotcom/elastic-job/issues/279) 添加事件追踪数据源,数据库连接地址不能带参数
1. [ISSUE #280](https://github.com/dangdangdotcom/elastic-job/issues/280) 作业历史页面的历史状态显示不正确
1. [ISSUE #283](https://github.com/dangdangdotcom/elastic-job/issues/283) 作业不设置overwrite且本地配置与注册中心不一致时，作业启动的cron应以注册中心为准
1. [ISSUE #290](https://github.com/dangdangdotcom/elastic-job/issues/290) Elastic-Job-Cloud删除被禁用的APP或JOB时，对应的disabled节点数据无法删除

## 2.1.0

### 新功能

1. [ISSUE #195](https://github.com/dangdangdotcom/elastic-job/issues/195) Elastic-Job-Lite自诊断并修复分布式不稳定造成的问题
1. [ISSUE #248](https://github.com/dangdangdotcom/elastic-job/issues/248) Elastic-Job-Lite同一台作业服务器可以行多个相同作业名称的JVM实例(Cloud Native)
1. [ISSUE #249](https://github.com/dangdangdotcom/elastic-job/issues/249) Elastic-Job-Lite运维界面支持事件追踪查询

### 功能提升

1. [ISSUE #240](https://github.com/dangdangdotcom/elastic-job/issues/240) Elastic-Job-Lite运维界面重构
1. [ISSUE #262](https://github.com/dangdangdotcom/elastic-job/issues/262) Elastic-Job-Lite控制台删除作业配置

### 缺陷修正

1. [ISSUE #237](https://github.com/dangdangdotcom/elastic-job/issues/238) 增加REST API对分片总数不小于1的校验
1. [ISSUE #238](https://github.com/dangdangdotcom/elastic-job/issues/238) IP正则表达式错误
1. [ISSUE #246](https://github.com/dangdangdotcom/elastic-job/issues/246) 通过JobOperateAPI.remove()后，再JobScheduler.init()创建相同作业后多次触发执行
1. [ISSUE #250](https://github.com/dangdangdotcom/elastic-job/issues/250) Misfire任务多触发一次

### 结构调整

1. [ISSUE #263](https://github.com/dangdangdotcom/elastic-job/issues/263) Elastic-Job-Lite作业操作API重新梳理
1. [ISSUE #264](https://github.com/dangdangdotcom/elastic-job/issues/264) Elastic-Job-Lite数据存储结构调整，但向前兼容

## 2.0.5

### 缺陷修正

1. [ISSUE 222](https://github.com/dangdangdotcom/elastic-job/issues/222) elastic-job-lite-spring的reg配置参数max-retries不起作用
1. [ISSUE 231](https://github.com/dangdangdotcom/elastic-job/issues/231) 批量删除cloud作业时，mesos会提前同步TASK_LOST消息给framework，导致作业被重新放入ready队列并执行

### 新功能

1. [ISSUE #191](https://github.com/dangdangdotcom/elastic-job/issues/191) Framework的HA功能
1. [ISSUE #217](https://github.com/dangdangdotcom/elastic-job/issues/217) cloud版本增加APP维度配置
1. [ISSUE #223](https://github.com/dangdangdotcom/elastic-job/issues/223) cloud版本常驻作业事件追踪采样率

## 2.0.4

### 缺陷修正

1. [ISSUE #189](https://github.com/dangdangdotcom/elastic-job/issues/189) 管理后台执行失效操作，但任务还在执行
1. [ISSUE #204](https://github.com/dangdangdotcom/elastic-job/issues/204) 异步事件执行消息顺序不一致导致数据库数据不准确
1. [ISSUE #209](https://github.com/dangdangdotcom/elastic-job/issues/209) cloud作业资源分配算法改进

### 新功能

1. [ISSUE #203](https://github.com/dangdangdotcom/elastic-job/issues/203) cloud类型作业增加运行统计，并提供REST API查询
1. [ISSUE #215](https://github.com/dangdangdotcom/elastic-job/issues/215) cloud版本运维管理界面

### 功能提升

1. [ISSUE #187](https://github.com/dangdangdotcom/elastic-job/issues/187) ShardingContext中增加taskId属性，供业务方使用 

## 2.0.3

### 缺陷修正

1. [ISSUE #177](https://github.com/dangdangdotcom/elastic-job/issues/177) 2.0.2版本Spring命名空间的job:script空指针
1. [ISSUE #185](https://github.com/dangdangdotcom/elastic-job/issues/185) Executor多占用分片资源导致资源浪费问题

### 新功能

1. [ISSUE #178](https://github.com/dangdangdotcom/elastic-job/issues/178) 事件驱动触发作业

### 功能提升

1. [ISSUE #179](https://github.com/dangdangdotcom/elastic-job/issues/179) Transient的Script类型作业优化，无需Java的Executor支持
1. [ISSUE #182](https://github.com/dangdangdotcom/elastic-job/issues/182) 增加对spring boot的支持

### 结构调整

1. [ISSUE #184](https://github.com/dangdangdotcom/elastic-job/issues/184) ExecutorServiceHandler接口方法调整，增加jobName区分用来区分不同作业线程名
1. [ISSUE #186](https://github.com/dangdangdotcom/elastic-job/issues/186) 去除Spring命名空间DTO相关代码，简化SpringJobScheduler使用

## 2.0.2

### 缺陷修正

1. [ISSUE #64](https://github.com/dangdangdotcom/elastic-job/issues/64) Spring命名空间，若注册多个同Class的作业Bean，会导致作业Bean查找不准确
1. [ISSUE #115](https://github.com/dangdangdotcom/elastic-job/issues/115) console新增注册中心，没有连接成功，后台一直反复连接并报错
1. [ISSUE #151](https://github.com/dangdangdotcom/elastic-job/issues/151) 基于关系型数据库的事件追踪缺乏对MySQL之外数据库的支持
1. [ISSUE #152](https://github.com/dangdangdotcom/elastic-job/issues/152) job自定义异常处理器无效，总是被DefaultJobExceptionHandler处理
1. [ISSUE #156](https://github.com/dangdangdotcom/elastic-job/issues/156) 作业事件追踪整体调用链路数据采集
1. [ISSUE #158](https://github.com/dangdangdotcom/elastic-job/issues/158) 作业在暂停时错过分片时机将不会再分片
1. [ISSUE #161](https://github.com/dangdangdotcom/elastic-job/issues/161) Lite版本部署至某些版本的Tomcat无法启动
1. [ISSUE #163](https://github.com/dangdangdotcom/elastic-job/issues/163) 任务设置disable＝true后，启动项目还是会自动执行任务
1. [ISSUE #165](https://github.com/dangdangdotcom/elastic-job/issues/165) 所有服务节点都disable时会导致分片线程死锁
1. [ISSUE #167](https://github.com/dangdangdotcom/elastic-job/issues/167) Failover作业增加源执行任务ID记录

### 功能提升

1. [ISSUE #159](https://github.com/dangdangdotcom/elastic-job/issues/159) 提供从Spring 3.1.0.REELASE至Spring 4任何版本的支持
1. [ISSUE #164](https://github.com/dangdangdotcom/elastic-job/issues/164) 作业Spring命名空间中已声明的JobBean不需要再声明@Component或在Spring xml中定义

### 结构调整

1. [ISSUE #153](https://github.com/dangdangdotcom/elastic-job/issues/153) 事件追踪配置集中化
1. [ISSUE #160](https://github.com/dangdangdotcom/elastic-job/issues/160) 调整maven模块结构，提供elastic-job-common及其二级模块，原elastic-job-core模块迁移至elastic-job-common-core

## 2.0.1

### 缺陷修正

1. [ISSUE #141](https://github.com/dangdangdotcom/elastic-job/issues/141) 删除reg模块从zk读取信息功能，使reg命名空间的placeholder完全可用
1. [ISSUE #143](https://github.com/dangdangdotcom/elastic-job/issues/143) elastic-job-cloud-scheduler内存泄漏问题
1. [ISSUE #145](https://github.com/dangdangdotcom/elastic-job/issues/145) 修改作业日志的数据库连接后日志还是会写入老的数据库
1. [ISSUE #146](https://github.com/dangdangdotcom/elastic-job/issues/146) 作业的线程池复用问题
1. [ISSUE #147](https://github.com/dangdangdotcom/elastic-job/issues/147) console作业维度加载不出来 后台有报空指针错误
1. [ISSUE #149](https://github.com/dangdangdotcom/elastic-job/issues/149) 运维平台删除作业，偶尔会遇到删除不全的情况
1. [ISSUE #150](https://github.com/dangdangdotcom/elastic-job/issues/150) Cloud的misfire功能在作业堆积时将会一直执行

## 2.0.0

### 新功能

1. Elastic-Job-Cloud初始版本
1. 重构原Elastic-Job至Elastic-Job-Lite

### 缺陷修正

1. [ISSUE #119](https://github.com/dangdangdotcom/elastic-job/issues/119) spring容器关闭时，quartz未正常关闭 
1. [ISSUE #123](https://github.com/dangdangdotcom/elastic-job/issues/123) 单机跑定时任务，zk断开后重连，没有触发leader选举 
1. [ISSUE #127](https://github.com/dangdangdotcom/elastic-job/issues/127) Spring方式配置作业id无法使用占位符

## 1.1.1

### 结构调整

1. [ISSUE #116](https://github.com/dangdangdotcom/elastic-job/issues/116) 作业接口的handleJobExecutionException参数变更

### 功能提升

1. [ISSUE #110](https://github.com/dangdangdotcom/elastic-job/issues/110) 手动触发作业

### 缺陷修正
1. [ISSUE #99](https://github.com/dangdangdotcom/elastic-job/issues/99) 删除作业异步导致作业删除后, 还未结束的作业继续创建zk数据

## 1.1.0

### 结构调整

1. [ISSUE #97](https://github.com/dangdangdotcom/elastic-job/issues/97) JobConfiguration重构为SimpleJobConfiguration，DataflowJobConfiguration，ScriptJobConfiguration
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
1. [ISSUE #60](https://github.com/dangdangdotcom/elastic-job/issues/60) 可于Dataflow类型作业定制化线程池配置
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
1. [ISSUE #28](https://github.com/dangdangdotcom/elastic-job/issues/28) Dataflow类型作业增加processData批量处理数据的方法
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
