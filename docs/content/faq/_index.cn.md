+++
pre = "<b>8. </b>"
title = "FAQ"
weight = 8
chapter = true
+++

## 1. 阅读源码时为什么会出现编译错误?

回答：

ElasticJob 使用 lombok 实现极简代码。关于更多使用和安装细节，请参考 [lombok 官网](https://projectlombok.org/download)。

## 2. 是否支持动态添加作业?

回答：

动态添加作业这个概念每个人理解不尽相同。

ElasticJob-Lite 为 jar 包，由开发或运维人员负责启动。启动时自动向注册中心注册作业信息并进行分布式协调，因此并不需要手工在注册中心填写作业信息。
但注册中心与作业部署机无从属关系，注册中心并不能控制将单点的作业分发至其他作业机，也无法将远程服务器未启动的作业启动。
ElasticJob-Lite 并不会包含 ssh 免密管理等功能。

ElasticJob-Cloud 为 mesos 框架，由 mesos 负责作业启动和分发。
但需要将作业打包上传，并调用 ElasticJob-Cloud 提供的 RESTful API 写入注册中心。
打包上传属于部署系统的范畴 ElasticJob-Cloud 并未涉及。

综上所述，ElasticJob 已做了基本动态添加功能，但无法做到真正意义的完全自动化添加。

## 3. 为什么在代码或配置文件中修改了作业配置，注册中心配置却没有更新?

回答：

ElasticJob-Lite 采用无中心化设计，若每个客户端的配置不一致，不做控制的话，最后一个启动的客户端配置将会成为注册中心的最终配置。

ElasticJob-Lite 提出了 overwrite 概念，可通过 JobConfiguration 或 Spring 命名空间配置。
`overwrite=true` 即允许客户端配置覆盖注册中心，反之则不允许。
如果注册中心无相关作业的配置，则无论 overwrite 是否配置，客户端配置都将写入注册中心。

## 4. 作业与注册中心无法通信会如何?

回答：

为了保证作业的在分布式场景下的一致性，一旦作业与注册中心无法通信，运行中的作业会立刻停止执行，但作业的进程不会退出。
这样做的目的是为了防止作业重分片时，将与注册中心失去联系的节点执行的分片分配给另外节点，导致同一分片在两个节点中同时执行。
当作业节点恢复与注册中心联系时，将重新参与分片并恢复执行新的分配到的分片。

## 5. ElasticJob-Lite 有何使用限制?

回答：

* 作业启动成功后修改作业名称视为新作业，原作业废弃。

* 一旦有服务器波动，或者修改分片项，将会触发重新分片；触发重新分片将会导致运行中的流式处理的作业在执行完本次作业后不再继续执行，等待分片结束后再恢复正常。

* 开启 monitorExecution 才能实现分布式作业幂等性（即不会在多个作业服务器运行同一个分片）的功能，但 monitorExecution 对短时间内执行的作业（如秒级触发）性能影响较大，建议关闭并自行实现幂等性。

## 6. 怀疑 ElasticJob-Lite 在分布式环境中有问题，但无法重现又不能在线上环境调试，应该怎么做?

回答：

分布式问题非常难于调试和重现，为此 ElasticJob-Lite 提供了 dump 命令。

如果您怀疑某些场景出现问题，可参照[作业信息导出](/cn/user-manual/elasticjob-lite/operation/dump/)将作业运行时信息提交至社区。
ElasticJob 已将 IP 地址等敏感信息过滤，导出的信息可在公网安全传输。

## 7. ElasticJob-Cloud 有何使用限制?

回答：

* 作业启动成功后修改作业名称视为新作业，原作业废弃。

## 8. 在 ElasticJob-Cloud 中添加任务后，为什么任务一直在 ready 状态，而不开始执行?

回答：

任务在 mesos 有单独的 agent 可提供所需的资源时才会启动，否则会等待直到有足够的资源。

## 9. 控制台界面无法正常显示?

回答：

使用控制台时应确保与 ElasticJob 相关版本保持一致，否则会导致不可用。

## 10. 为什么控制台界面中的作业状态是分片待调整?

回答：

分片待调整表示作业已启动但尚未获得分片时的状态。

## 11. 为什么首次启动存在任务调度延迟的情况？

回答：
ElasticJob 执行任务会获取本机IP，首次可能存在获取IP较慢的情况。尝试设置-Djava.net.preferIPv4Stack=true.


## 12. Windows环境下，运行ShardingSphere-ElasticJob-UI，找不到或无法加载主类 org.apache.shardingsphere.elasticjob.lite.ui.Bootstrap，如何解决？

回答：

某些解压缩工具在解压ShardingSphere-ElasticJob-UI二进制包时可能将文件名截断，导致找不到某些类。

解决方案：

打开cmd.exe并执行下面的命令：

tar zxvf apache-shardingsphere-elasticjob-${RELEASE.VERSION}-lite-ui-bin.tar.gz

## 13. 运行 Cloud Scheduler 持续输出日志 "Elastic job: IP:PORT has leadership"，不能正常运行

回答：

Cloud Scheduler 依赖 Mesos 库，启动时需要通过 `-Djava.library.path` 指定 Mesos 库所在目录。

例如，Mesos 库位于 `/usr/local/lib`，启动 Cloud Scheduler 前需要设置 `-Djava.library.path=/usr/local/lib`。

Mesos 相关请参考 [Apache Mesos](https://mesos.apache.org/)。
