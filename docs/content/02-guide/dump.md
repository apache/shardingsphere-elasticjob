+++
toc = true
date = "2016-01-27T16:14:21+08:00"
title = "dump作业运行信息"
weight = 24
prev = "/02-guide/execution-monitor/"
next = "/02-guide/job-listener/"
+++

使用Elastic-Job-Lite过程中可能会碰到一些分布式问题，导致作业运行不稳定。

由于无法在生产环境调试，通过dump命令可以把作业内部相关信息dump出来，方便开发者debug分析；
另外为了不泄露隐私，已将相关信息中的ip地址以ip1, ip2...的形式过滤，可以在互联网上公开传输环境信息，便于进一步完善Elastic-Job。

## 如何使用

### 配置监听端口

支持两种配置方式

* Spring方式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:reg="http://elasticjob.shardingsphere.apache.org/schema/reg"
        xmlns:job="http://elasticjob.shardingsphere.apache.org/schema/job"
        xmlns:monitor="http://elasticjob.shardingsphere.apache.org/schema/monitor"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
                            http://www.springframework.org/schema/beans/spring-beans.xsd
                            http://elasticjob.shardingsphere.apache.org/schema/reg
                            http://elasticjob.shardingsphere.apache.org/schema/reg/reg.xsd
                            http://elasticjob.shardingsphere.apache.org/schema/job
                            http://elasticjob.shardingsphere.apache.org/schema/job/job.xsd
                            http://elasticjob.shardingsphere.apache.org/schema/monitor
                            http://elasticjob.shardingsphere.apache.org/schema/monitor/monitor.xsd
                        ">
    <!--配置作业注册中心 -->
    <reg:zookeeper id="regCenter" server-lists="yourhost:2181" namespace="dd-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
    
    <!--配置任务监控 -->
    <monitor:embed id="monitor1" registry-center-ref="regCenter" monitor-port="9999"/>    
    
    <!--配置作业类 -->
    <bean id="simpleJob" class="xxx.MyElasticJob" />    

    <!-- 配置作业-->
    <job:simple id="oneOffElasticJob" job-ref="simpleJob" registry-center-ref="regCenter" cron="0/10 * * * * ?"   sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
</beans>
```

* Java方式

```java
public class JobMain {
    public static void main(final String[] args) {
        // ...
        MonitorService monitorService = new MonitorService(regCenter, 9888);
        monitorService.listen();
        // ...
    }
}
```

### 启动作业

### 执行dump命令

dump命令完全参照Zookeeper的四字命令理念

```bash
echo "dump@jobName" | nc <任意一台作业服务器IP> 9888
```

![dump命令](https://shardingsphere.apache.org/elasticjob/lite/img/dump/dump.jpg)

导出至文件

```bash
echo "dump@jobName" | nc <任意一台作业服务器IP> 9888 > job_debug.txt
```

## 使用注意事项

务必更新至1.0.3以上版本
