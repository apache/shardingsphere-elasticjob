+++
title = "Java API"
weight = 1
chapter = true
+++

## 注册中心配置

用于注册和协调作业分布式行为的组件，目前仅支持 ZooKeeper。

类名称：org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration

可配置属性：

| 属性名                         | 类型     | 构造器注入 | 缺省值 | 描述 |
| ----------------------------- |:-------- |:-------- |:------ |:--- |
| serverLists                   | String   | 是       |        | 连接 ZooKeeper 服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: host1:2181,host2:2181 |
| namespace                     | String   | 是       |        | ZooKeeper 的命名空间 |
| baseSleepTimeMilliseconds     | int      | 否       | 1000   | 等待重试的间隔时间的初始毫秒数 |
| maxSleepTimeMilliseconds      | String   | 否       | 3000   | 等待重试的间隔时间的最大毫秒数 |
| maxRetries                    | String   | 否       | 3      | 最大重试次数 |
| sessionTimeoutMilliseconds    | boolean  | 否       | 60000  | 会话超时毫秒数 |
| connectionTimeoutMilliseconds | boolean  | 否       | 15000  | 连接超时毫秒数 |
| digest                        | String   | 否       |        | 连接 ZooKeeper 的权限令牌<br />缺省为不需要权限验证 |

## 作业配置

类名称：org.apache.shardingsphere.elasticjob.api.JobConfiguration

| 属性名                         | 类型       | 构造器注入 | 缺省值         | 描述     |
| ----------------------------- |:---------- |:-------- |:-------------- |:------- |
| jobName                       | String     | 是       |                | 作业名称 |
| cron                          | String     | 是       |                | CRON 表达式，用于控制作业触发时间 |
| shardingTotalCount            | int        | 是       |                | 作业分片总数 |
| shardingItemParameters        | String     | 否       |                | 分片序列号和参数用等号分隔，多个键值对用逗号分隔<br />分片序列号从0开始，不可大于或等于作业分片总数<br />如：<br/>0=a,1=b,2=c |
| jobParameter                  | String     | 否       |                | 作业自定义参数<br />作业自定义参数，可通过传递该参数为作业调度的业务方法传参，用于实现带参数的作业<br />例：每次获取的数据量、作业实例从数据库读取的主键等 |
| monitorExecution              | boolean    | 否       | true           | 监控作业运行时状态<br />每次作业执行时间和间隔时间均非常短的情况，建议不监控作业运行时状态以提升效率。因为是瞬时状态，所以无必要监控。请用户自行增加数据堆积监控。并且不能保证数据重复选取，应在作业中实现幂等性。<br />每次作业执行时间和间隔时间均较长的情况，建议监控作业运行时状态，可保证数据不会重复选取。 |
| failover                      | boolean    | 否       | false          | 是否开启任务执行失效转移，开启表示如果作业在一次任务执行中途宕机，允许将该次未完成的任务在另一作业节点上补偿执行 |
| misfire                       | boolean    | 否       | true           | 是否开启错过任务重新执行 |
| maxTimeDiffSeconds            | int        | 否       | -1             | 最大允许的本机与注册中心的时间误差秒数<br />如果时间误差超过配置秒数则作业启动时将抛异常<br />配置为-1表示不校验时间误差 |
| reconcileIntervalMinutes      | int        | 否       | 10             | 修复作业服务器不一致状态服务调度间隔时间，配置为小于1的任意值表示不执行修复<br />单位：分钟 |
| jobShardingStrategyType       | String     | 否       | AVG_ALLOCATION | 作业分片策略类型<br />策略类型的详情，请参见[内置分片策略列表](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/sharding)         |
| jobExecutorServiceHandlerType | String     | 否       | CPU            | 作业线程池处理策略<br />策略类型的详情，请参见[内置线程池策略列表](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/thread-pool)   |
| jobErrorHandlerType           | String     | 否       |                | 作业错误处理策略<br />策略类型的详情，请参见[内置错误处理策略列表](/cn/user-manual/elasticjob-lite/configuration/built-in-strategy/error-handler) |
| description                   | String     | 否       |                | 作业描述信息 |
| props                         | Properties | 否       |                | 作业属性配置信息，对于 Dataflow 类型任务，配置 streaming.process=true 开启流式处理任务。对于 Script 类型任务，配置 script.command.line 指定运行脚本 |
| disabled                      | boolean    | 否       | false          | 作业是否禁止启动<br />可用于部署作业时，先禁止启动，部署结束后统一启动      |
| overwrite                     | boolean    | 否       | false          | 本地配置是否可覆盖注册中心配置<br />如果可覆盖，每次启动作业都以本地配置为准 |
