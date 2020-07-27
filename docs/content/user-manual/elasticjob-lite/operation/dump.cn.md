+++
title = "导出作业信息"
weight = 2
chapter = true
+++

使用 ElasticJob-Lite 过程中可能会碰到一些分布式问题，导致作业运行不稳定。

由于无法在生产环境调试，通过 dump 命令可以把作业内部相关信息导出，方便开发者调试分析；
另外为了不泄露隐私，已将相关信息中的 IP 地址以 ip1, ip2... 的形式过滤，可以在互联网上公开传输环境信息，便于进一步完善 ElasticJob。

## 开启监听端口

使用 Java 开启导出端口配置请参见[Java API 使用指南](/cn/user-manual/elasticjob-lite/usage/job-api/java-api)。
使用 Spring 开启导出端口配置请参见[Spring 使用指南](/cn/user-manual/elasticjob-lite/usage/job-api/spring-namespace)。

## 执行导出命令

导出命令完全参照 ZooKeeper 的四字命令理念。

**导出至标准输出**

```bash
echo "dump@jobName" | nc <任意一台作业服务器IP> 9888
```

![导出命令](https://shardingsphere.apache.org/elasticjob/current/img/dump/dump.jpg)

**导出至文件**

```bash
echo "dump@jobName" | nc <任意一台作业服务器IP> 9888 > job_debug.txt
```
