+++
title = "Dump 作业运行信息"
weight = 2
chapter = true
+++

使用 ElasticJob-Lite 过程中可能会碰到一些分布式问题，导致作业运行不稳定。

由于无法在生产环境调试，通过 dump 命令可以把作业内部相关信息导出，方便开发者调试分析；
另外为了不泄露隐私，已将相关信息中的 IP 地址以 ip1, ip2... 的形式过滤，可以在互联网上公开传输环境信息，便于进一步完善 ElasticJob。

## 如何使用

### 配置监听端口

支持 2 种配置方式

* Spring 方式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:elasticjob="http://shardingsphere.apache.org/schema/elasticjob"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/elasticjob
                           http://shardingsphere.apache.org/schema/elasticjob/elasticjob.xsd
                         ">
    <!--配置作业注册中心 -->
    <elasticjob:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="dd-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
    
    <!--配置任务快照 -->
    <elasticjob:snapshot id="jobSnapshot" registry-center-ref="regCenter" dump-port="9999"/>    
    
    <!--配置作业类 -->
    <bean id="simpleJob" class="xxx.MyElasticJob" />    

    <!-- 配置作业-->
    <elasticjob:simple id="oneOffElasticJob" job-ref="simpleJob" registry-center-ref="regCenter" cron="0/10 * * * * ?"   sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
</beans>
```

* Java 方式

```java
public class JobMain {
    
    public static void main(final String[] args) {
        // ...
        SnapshotService snapshotService = new SnapshotService(regCenter, 9888);
        snapshotService.listen();
        // ...
    }
}
```

### 启动作业

### 执行 dump 命令

dump 命令完全参照 Zookeeper 的四字命令理念

```bash
echo "dump@jobName" | nc <任意一台作业服务器IP> 9888
```

![dump命令](https://shardingsphere.apache.org/elasticjob/current/img/dump/dump.jpg)

导出至文件

```bash
echo "dump@jobName" | nc <任意一台作业服务器IP> 9888 > job_debug.txt
```

## 使用注意事项

务必更新至 1.0.3 以上版本。
