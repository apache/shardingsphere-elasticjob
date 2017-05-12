+++
toc = true
date = "2016-03-21T16:14:21+08:00"
title = "配置手册"
weight = 12
prev = "/02-guide/web-console/"
next = "/02-guide/job-sharding-strategy/"
+++

## 1. Java Code配置

### a. 注册中心配置

用于注册和协调作业分布式行为的组件，目前仅支持Zookeeper。

#### ZookeeperConfiguration属性详细说明

| 属性名                         | 类型    | 构造器注入 | 缺省值 | 描述 |
| ------------------------------|:--------|:---------|:-------|:----|
| serverLists                   | String  | 是     |        | 连接Zookeeper服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: host1:2181,host2:2181 |
| namespace                     | String  | 是     |        | Zookeeper的命名空间 |
| baseSleepTimeMilliseconds     | int     | 否       | 1000   | 等待重试的间隔时间的初始值<br />单位：毫秒 |
| maxSleepTimeMilliseconds      | String  | 否       | 3000   | 等待重试的间隔时间的最大值<br />单位：毫秒 |
| maxRetries                    | String  | 否       | 3      | 最大重试次数 |
| sessionTimeoutMilliseconds    | boolean | 否       | 60000  | 会话超时时间<br />单位：毫秒 |
| connectionTimeoutMilliseconds | boolean | 否       | 15000  | 连接超时时间<br />单位：毫秒 |
| digest                        | String  | 否       |        | 连接Zookeeper的权限令牌<br />缺省为不需要权限验证 |

### b. 作业配置

作业配置分为3级，分别是JobCoreConfiguration，JobTypeConfiguration和LiteJobConfiguration。LiteJobConfiguration使用JobTypeConfiguration，JobTypeConfiguration使用JobCoreConfiguration，层层嵌套。
JobTypeConfiguration根据不同实现类型分为SimpleJobConfiguration，DataflowJobConfiguration和ScriptJobConfiguration。

#### JobCoreConfiguration属性详细说明

| 属性名                  | 类型    | 构造器注入 | 缺省值 | 描述     |
| -----------------------|:--------|:---------|:-------|:--------|
| jobName                | String  | 是       |        | 作业名称 |
| cron                   | String  | 是       |        | cron表达式，用于控制作业触发时间 |
| shardingTotalCount     | int     | 是       |        | 作业分片总数 |
| shardingItemParameters | String  | 否       |        | 分片序列号和参数用等号分隔，多个键值对用逗号分隔<br />分片序列号从0开始，不可大于或等于作业分片总数<br />如：<br/>0=a,1=b,2=c |
| jobParameter           | String  | 否       |        | 作业自定义参数<br />作业自定义参数，可通过传递该参数为作业调度的业务方法传参，用于实现带参数的作业<br />例：每次获取的数据量、作业实例从数据库读取的主键等 |
| failover               | boolean | 否       | false  | 是否开启任务执行失效转移，开启表示如果作业在一次任务执行中途宕机，允许将该次未完成的任务在另一作业节点上补偿执行 |
| misfire                | boolean | 否       | true   | 是否开启错过任务重新执行 |
| description            | String  | 否       |        | 作业描述信息 |
| jobProperties          | Enum    | 否       |        | 配置jobProperties定义的枚举控制Elastic-Job的实现细节<br />JOB_EXCEPTION_HANDLER用于扩展异常处理类<br />EXECUTOR_SERVICE_HANDLER用于扩展作业处理线程池类|

#### SimpleJobConfiguration属性详细说明

| 属性名      | 类型                 | 构造器注入 | 缺省值 | 描述                             |
| -----------|:---------------------|:---------|:-------|:--------------------------------|
| coreConfig | JobCoreConfiguration | 是     |        |                                 |
| jobClass   | String               | 是     |        | 作业实现类，需实现ElasticJob接口 |

#### DataflowJobConfiguration属性详细说明

| 属性名            | 类型                 | 构造器注入 | 缺省值         | 描述                            |
| -----------------|:---------------------|:---------|:--------------|:--------------------------------|
| coreConfig       | JobCoreConfiguration | 是     |               |                                 |
| jobClass         | String               | 是     |               | 作业实现类，需实现ElasticJob接口 |
| streamingProcess | boolean              | 否       | false         | 是否流式处理数据<br />如果流式处理数据, 则fetchData不返回空结果将持续执行作业<br />如果非流式处理数据, 则处理数据完成后作业结束 |

#### ScriptJobConfiguration属性详细说明

| 属性名             | 类型                 | 构造器注入 | 缺省值 | 描述               |
| ------------------|:---------------------|:---------|:-------|:------------------|
| coreConfig        | JobCoreConfiguration | 是     |        |                   |
| scriptCommandLine | String               | 是     |        | 脚本型作业执行命令行 |

#### LiteJobConfiguration属性详细说明

| 属性名                    | 类型                 | 构造器注入 | 缺省值           | 描述                                                   |
| -------------------------|:---------------------|:---------|:----------------|:-------------------------------------------------------|
| jobConfig                | JobTypeConfiguration | 是     |                 |                                                        |
| monitorExecution         | boolean              | 否       |true             | 监控作业运行时状态<br />每次作业执行时间和间隔时间均非常短的情况，建议不监控作业运行时状态以提升效率。因为是瞬时状态，所以无必要监控。请用户自行增加数据堆积监控。并且不能保证数据重复选取，应在作业中实现幂等性。<br />每次作业执行时间和间隔时间均较长的情况，建议监控作业运行时状态，可保证数据不会重复选取。 |
| monitorPort              | int                  | 否       |-1               | 作业监控端口<br />建议配置作业监控端口, 方便开发者dump作业信息。<br />使用方法: echo "dump" \| nc 127.0.0.1 9888 |
| maxTimeDiffSeconds       | int                  | 否       |-1               | 最大允许的本机与注册中心的时间误差秒数<br />如果时间误差超过配置秒数则作业启动时将抛异常<br />配置为-1表示不校验时间误差 |
| jobShardingStrategyClass | String               | 否       |-1               | 作业分片策略实现类全路径<br />默认使用平均分配策略<br />详情参见：[作业分片策略](/02-guide/job-sharding-strategy) |
| reconcileIntervalMinutes | int                  | 否       |10               | 修复作业服务器不一致状态服务调度间隔时间，配置为小于1的任意值表示不执行修复<br />单位：分钟 |
| eventTraceRdbDataSource  | String               | 否       |                 | 作业事件追踪的数据源Bean引用 |

## 2. Spring命名空间配置

Spring命名空间与Java Code方式配置类似，大部分属性只是将命名方式由驼峰式改为以减号间隔。使用Spring命名空间需在pom.xml文件中添加elastic-job-lite-spring模块的依赖。

```xml
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-spring</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### a. 注册中心配置

#### reg:zookeeper命名空间属性详细说明

| 属性名                           | 类型   | 是否必填 | 缺省值 | 描述                                                                                               |
| ------------------------------- |:-------|:-------|:------|:---------------------------------------------------------------------------------------------------|
| id                              | String | 是   |       | 注册中心在Spring容器中的主键                                                                         |
| server-lists                    | String | 是   |       | 连接Zookeeper服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: host1:2181,host2:2181 |
| namespace                       | String | 是   |       | Zookeeper的命名空间                                                                                |
| base-sleep-time-milliseconds    | int    | 否     | 1000  | 等待重试的间隔时间的初始值<br />单位：毫秒                                                               |
| max-sleep-time-milliseconds     | int    | 否     | 3000  | 等待重试的间隔时间的最大值<br />单位：毫秒                                                               |
| max-retries                     | int    | 否     | 3     | 最大重试次数                                                                                          |
| session-timeout-milliseconds    | int    | 否     | 60000 | 会话超时时间<br />单位：毫秒                                                                           |
| connection-timeout-milliseconds | int    | 否     | 15000 | 连接超时时间<br />单位：毫秒                                                                           |
| digest                          | String | 否     |       | 连接Zookeeper的权限令牌<br />缺省为不需要权限验证                                                      |


### b. 作业配置

#### job:simple命名空间属性详细说明

| 属性名                               | 类型    | 是否必填 | 缺省值           | 描述                                                                       |
| ------------------------------------|:--------|:--------|:----------------|:---------------------------------------------------------------------------|
| id                                  | String  | 是      |                 | 作业名称                                                                    |
| class                               | String  | 否      |                 | 作业实现类，需实现ElasticJob接口                                              |
| registry-center-ref                 | String  | 是      |                 | 注册中心Bean的引用，需引用reg:zookeeper的声明                                  |
| cron                                | String  | 是      |                 | cron表达式，用于控制作业触发时间                                               |
| sharding-total-count                | int     | 是      |                 | 作业分片总数                                                                 |
| sharding-item-parameters            | String  | 否      |                 | 分片序列号和参数用等号分隔，多个键值对用逗号分隔<br />分片序列号从0开始，不可大于或等于作业分片总数<br />如：<br/>0=a,1=b,2=c|
| job-instance-id                     | String  | 否      | defaultInstance | 作业实例主键，同IP可运行实例主键不同, 但名称相同的多个作业实例                     |
| job-parameter                       | String  | 否      |                 | 作业自定义参数<br />作业自定义参数，可通过传递该参数为作业调度的业务方法传参，用于实现带参数的作业<br />例：每次获取的数据量、作业实例从数据库读取的主键等 |
| monitor-execution                   | boolean | 否      | true            | 监控作业运行时状态<br />每次作业执行时间和间隔时间均非常短的情况，建议不监控作业运行时状态以提升效率。因为是瞬时状态，所以无必要监控。请用户自行增加数据堆积监控。并且不能保证数据重复选取，应在作业中实现幂等性。<br />每次作业执行时间和间隔时间均较长的情况，建议监控作业运行时状态，可保证数据不会重复选取。|
| monitor-port                        | int     | 否      | -1              | 作业监控端口<br />建议配置作业监控端口, 方便开发者dump作业信息。<br />使用方法: echo "dump" \| nc 127.0.0.1 9888|
| max-time-diff-seconds               | int     | 否      | -1              | 最大允许的本机与注册中心的时间误差秒数<br />如果时间误差超过配置秒数则作业启动时将抛异常<br />配置为-1表示不校验时间误差|
| failover                            | boolean | 否      | false           | 是否开启失效转移                                                              |
| misfire                             | boolean | 否      | true            | 是否开启错过任务重新执行                                                       |
| job-sharding-strategy-class         | String  | 否      |                 | 作业分片策略实现类全路径<br />默认使用平均分配策略<br />详情参见：[作业分片策略](/02-guide/job-sharding-strategy)|
| description                         | String  | 否      |                 | 作业描述信息                                                                 |
| disabled                            | boolean | 否      | false           | 作业是否禁止启动<br />可用于部署作业时，先禁止启动，部署结束后统一启动              |
| overwrite                           | boolean | 否      | false           | 本地配置是否可覆盖注册中心配置<br />如果可覆盖，每次启动作业都以本地配置为准         |
| job-exception-handler               | String  | 否      |                 | 扩展异常处理类                                                               |
| executor-service-handler            | String  | 否      |                 | 扩展作业处理线程池类                                                          |
| reconcile-interval-minutes          | int     | 否      | 10              | 修复作业服务器不一致状态服务调度间隔时间，配置为小于1的任意值表示不执行修复<br />单位：分钟 |
| event-trace-rdb-data-source         | String  | 否      |                 | 作业事件追踪的数据源Bean引用                                                   |

#### job:dataflow命名空间属性详细说明

job:dataflow命名空间拥有job:simple命名空间的全部属性，以下仅列出特有属性

| 属性名             | 类型    | 是否必填 | 缺省值    | 描述                                                                                                                |
| ----------------- |:--------|:-------|:----------|:-------------------------------------------------------------------------------------------------------------------|
| streaming-process | boolean | 否     | false     | 是否流式处理数据<br />如果流式处理数据, 则fetchData不返回空结果将持续执行作业<br />如果非流式处理数据, 则处理数据完成后作业结束 |

#### job:script命名空间属性详细说明，基本属性参照job:simple命名空间属性详细说明

job:script命名空间拥有job:simple命名空间的全部属性，以下仅列出特有属性

| 属性名                            | 类型   | 是否必填 | 缺省值 | 描述               |
| -------------------------------- |:-------|:--------|:------|:------------------|
| script-command-line              | String | 否      |       | 脚本型作业执行命令行 |

#### job:listener命名空间属性详细说明

job:listener必须配置为job:bean的子元素，并且在子元素中只允许出现一次

| 属性名                          | 类型   | 是否必填 | 缺省值        | 描述                                              |
| ------------------------------ |:-------|:-------|:--------------|:--------------------------------------------------|
| class                          | String |是    |               | 前置后置任务监听实现类，需实现ElasticJobListener接口 |

### c. 作业监听配置

#### job:distributed-listener命名空间属性详细说明

job:distributed-listener必须配置为job:bean的子元素，并且在子元素中只允许出现一次

| 属性名                          | 类型   | 是否必填 | 缺省值         | 描述                                                                        |
| ------------------------------ |:-------|:-------|:---------------|:---------------------------------------------------------------------------|
| class                          | String | 是   |                | 前置后置任务分布式监听实现类，需继承AbstractDistributeOnceElasticJobListener类 |
| started-timeout-milliseconds   | long   | 否     | Long.MAX_VALUE | 最后一个作业执行前的执行方法的超时时间<br />单位：毫秒                            |
| completed-timeout-milliseconds | long   | 否     | Long.MAX_VALUE | 最后一个作业执行后的执行方法的超时时间<br />单位：毫秒                            |
