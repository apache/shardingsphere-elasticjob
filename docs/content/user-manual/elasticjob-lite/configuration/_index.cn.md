+++
pre = "<b>4.1.2 </b>"
title = "配置手册"
weight = 2
chapter = true
+++

## 简介

通过配置可以快速清晰的理解 ElasticJob-Lite 所提供的功能。

本章节是 ElasticJob-Lite 的配置参考手册，需要时可当做字典查阅。

ElasticJob-Lite 提供了 3 种配置方式，用于不同的使用场景。

## 配置项说明

### 注册中心配置

| 属性名                         | 类型     | 缺省值 | 描述 |
| ----------------------------- |:-------- |:------ |:--- |
| serverLists                   | String   |        | 连接 ZooKeeper 服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: host1:2181,host2:2181 |
| namespace                     | String   |        | ZooKeeper 的命名空间 |
| baseSleepTimeMilliseconds     | int      |  1000  | 等待重试的间隔时间的初始毫秒数 |
| maxSleepTimeMilliseconds      | String   |  3000  | 等待重试的间隔时间的最大毫秒数 |
| maxRetries                    | String   |  3     | 最大重试次数 |
| sessionTimeoutMilliseconds    | boolean  |  60000 | 会话超时毫秒数 |
| connectionTimeoutMilliseconds | boolean  |  15000 | 连接超时毫秒数 |
| digest                        | String   |        | 连接 ZooKeeper 的权限令牌<br />缺省为不需要权限验证 |

### 作业配置

| 属性名                         | 类型       | 缺省值          | 描述     |
| ----------------------------- |:---------- |:-------------- |:------- |
| jobName                       | String     |                | 作业名称 |
| shardingTotalCount            | int        |                | 作业分片总数 |
| cron                          | String     |                | CRON 表达式，用于控制作业触发时间 |
| shardingItemParameters        | String     |                | 分片序列号和参数用等号分隔，多个键值对用逗号分隔<br />分片序列号从0开始，不可大于或等于作业分片总数<br />如：<br/>0=a,1=b,2=c |
| jobParameter                  | String     |                | 作业自定义参数<br />作业自定义参数，可通过传递该参数为作业调度的业务方法传参，用于实现带参数的作业<br />例：每次获取的数据量、作业实例从数据库读取的主键等 |
| monitorExecution              | boolean    | true           | 监控作业运行时状态<br />每次作业执行时间和间隔时间均非常短的情况，建议不监控作业运行时状态以提升效率。因为是瞬时状态，所以无必要监控。请用户自行增加数据堆积监控。并且不能保证数据重复选取，应在作业中实现幂等性。<br />每次作业执行时间和间隔时间均较长的情况，建议监控作业运行时状态，可保证数据不会重复选取。 |
| failover                      | boolean    | false          | 是否开启任务执行失效转移，开启表示如果作业在一次任务执行中途宕机，允许将该次未完成的任务在另一作业节点上补偿执行 |
| misfire                       | boolean    | true           | 是否开启错过任务重新执行 |
| maxTimeDiffSeconds            | int        | -1             | 最大允许的本机与注册中心的时间误差秒数<br />如果时间误差超过配置秒数则作业启动时将抛异常<br />配置为-1表示不校验时间误差 |
| reconcileIntervalMinutes      | int        | 10             | 修复作业服务器不一致状态服务调度间隔时间，配置为小于1的任意值表示不执行修复<br />单位：分钟 |
| jobShardingStrategyType       | String     | AVG_ALLOCATION | 作业分片策略类型<br />策略类型的详情，请参见[内置分片策略列表](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/sharding)         |
| jobExecutorServiceHandlerType | String     | CPU            | 作业线程池处理策略<br />策略类型的详情，请参见[内置线程池策略列表](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/thread-pool)   |
| jobErrorHandlerType           | String     |                | 作业错误处理策略<br />策略类型的详情，请参见[内置错误处理策略列表](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler) |
| description                   | String     |                | 作业描述信息 |
| props                         | Properties |                | 作业属性配置信息<br />属性配置的详情，请参见[作业属性配置列表](/cn/user-manual/elasticjob-lite/configuration/props) |
| disabled                      | boolean    | false          | 作业是否禁止启动<br />可用于部署作业时，先禁止启动，部署结束后统一启动      |
| overwrite                     | boolean    | false          | 本地配置是否可覆盖注册中心配置<br />如果可覆盖，每次启动作业都以本地配置为准 |
