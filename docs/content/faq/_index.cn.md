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

ElasticJob 为 jar 包，由开发或运维人员负责启动。启动时自动向注册中心注册作业信息并进行分布式协调，因此并不需要手工在注册中心填写作业信息。
但注册中心与作业部署机无从属关系，注册中心并不能控制将单点的作业分发至其他作业机，也无法将远程服务器未启动的作业启动。
ElasticJob 并不会包含 ssh 免密管理等功能。

综上所述，ElasticJob 已做了基本动态添加功能，但无法做到真正意义的完全自动化添加。

## 3. 为什么在代码或配置文件中修改了作业配置，注册中心配置却没有更新?

回答：

ElasticJob 采用无中心化设计，若每个客户端的配置不一致，不做控制的话，最后一个启动的客户端配置将会成为注册中心的最终配置。

ElasticJob 提出了 overwrite 概念，可通过 JobConfiguration 或 Spring 命名空间配置。
`overwrite=true` 即允许客户端配置覆盖注册中心，反之则不允许。
如果注册中心无相关作业的配置，则无论 overwrite 是否配置，客户端配置都将写入注册中心。

## 4. 作业与注册中心无法通信会如何?

回答：

为了保证作业的在分布式场景下的一致性，一旦作业与注册中心无法通信，运行中的作业会立刻停止执行，但作业的进程不会退出。
这样做的目的是为了防止作业重分片时，将与注册中心失去联系的节点执行的分片分配给另外节点，导致同一分片在两个节点中同时执行。
当作业节点恢复与注册中心联系时，将重新参与分片并恢复执行新的分配到的分片。

## 5. ElasticJob 有何使用限制?

回答：

* 作业启动成功后修改作业名称视为新作业，原作业废弃。

* 一旦有服务器波动，或者修改分片项，将会触发重新分片；触发重新分片将会导致运行中的流式处理的作业在执行完本次作业后不再继续执行，等待分片结束后再恢复正常。

* 开启 monitorExecution 才能实现分布式作业幂等性（即不会在多个作业服务器运行同一个分片）的功能，但 monitorExecution 对短时间内执行的作业（如秒级触发）性能影响较大，建议关闭并自行实现幂等性。

## 6. 怀疑 ElasticJob 在分布式环境中有问题，但无法重现又不能在线上环境调试，应该怎么做?

回答：

分布式问题非常难于调试和重现，为此 ElasticJob 提供了 dump 命令。

如果您怀疑某些场景出现问题，可参照[作业信息导出](/cn/user-manual/elasticjob/operation/dump/)将作业运行时信息提交至社区。
ElasticJob 已将 IP 地址等敏感信息过滤，导出的信息可在公网安全传输。

## 7. 控制台界面无法正常显示?

回答：

使用控制台时应确保与 ElasticJob 相关版本保持一致，否则会导致不可用。

## 8. 为什么控制台界面中的作业状态是分片待调整?

回答：

分片待调整表示作业已启动但尚未获得分片时的状态。

## 9. 为什么首次启动存在任务调度延迟的情况？

回答：
ElasticJob 执行任务会获取本机IP，首次可能存在获取IP较慢的情况。尝试设置 `-Djava.net.preferIPv4Stack=true`.


## 10. Windows环境下，运行ShardingSphere-ElasticJob-UI，找不到或无法加载主类 org.apache.shardingsphere.elasticjob.engine.ui.Bootstrap，如何解决？

回答：

某些解压缩工具在解压ShardingSphere-ElasticJob-UI二进制包时可能将文件名截断，导致找不到某些类。

解决方案：

打开cmd.exe并执行下面的命令：

```bash
tar zxvf apache-shardingsphere-elasticjob-${RELEASE.VERSION}-lite-ui-bin.tar.gz
```

## 11. 运行 Cloud Scheduler 持续输出日志 "Elastic job: IP:PORT has leadership"，不能正常运行

回答：

Cloud Scheduler 依赖 Mesos 库，启动时需要通过 `-Djava.library.path` 指定 Mesos 库所在目录。

例如，Mesos 库位于 `/usr/local/lib`，启动 Cloud Scheduler 前需要设置 `-Djava.library.path=/usr/local/lib`。

Mesos 相关请参考 [Apache Mesos](https://mesos.apache.org/)。

## 12. 在多网卡的情况下无法获取到合适的 IP

回答：

可以通过系统变量 `elasticjob.preferred.network.interface` 指定网卡或 `elasticjob.preferred.network.ip` 指定IP地址。

例如:

1. 指定网卡 eno1：`-Delasticjob.preferred.network.interface=eno1`。
1. 指定IP地址 192.168.0.100：`-Delasticjob.preferred.network.ip=192.168.0.100`。
1. 泛指IP地址(正则表达式) 192.168.*：`-Delasticjob.preferred.network.ip=192.168.*`。

## 13. zk授权升级,在滚动部署过程中出现实例假死,回退到历史版本也依然存在假死。

回答:

在滚动部署过程中,会触发竞争选举leader,有密码的实例会给zk目录加密导致无密码的实例不可访问,最终导致整体选举阻塞。

例如:

通过日志可以发现会抛出-102异常:

```bash
xxxx-07-27 22:33:55.224 [DEBUG] [localhost-startStop-1-EventThread] [] [] [] - o.a.c.f.r.c.TreeCache : processResult: CuratorEventImpl{type=GET_DATA, resultCode=-102, path='/xxx/leader/election/latch/_c_bccccdcc-1134-4e0a-bb52-59a13836434a-latch-0000000047', name='null', children=null, context=null, stat=null, data=null, watchedEvent=null, aclList=null}
```

解决方案:

1.如果您在升级的过程中出现回退历史版本也依然假死的问题,建议删除zk上所有作业目录,之后再重启历史版本。
2.计算出合理的作业执行间隙,比如晚上21:00-21:30作业不会触发,在此期间先将实例全部停止,然后将带密码的版本全部部署上线。
