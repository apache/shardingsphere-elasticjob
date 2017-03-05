+++
date = "2016-01-27T16:14:21+08:00"
title = "1.x dump作业运行信息（便于开发者debug）"
weight=1013
+++

# dump作业运行信息（便于开发者debug）

由于在使用`elastic-job`中可能会碰到一些分布式问题，导致作业运行不稳定。由于无法在生产环境调试，通过`dump`命令可以把作业内部相关信息`dump`出来，方便开发者`debug`分析；另外为了不泄露隐私，已经把相关信息中的`ip`地址以`ip1, ip2...`的形式过滤掉了，可以在互联网上公开传输环境信息，便于进一步完善`elastic-job`。

## 如何使用

### 配置监听端口

支持两种配置方式

* Spring方式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:reg="http://www.dangdang.com/schema/ddframe/reg"
    xmlns:job="http://www.dangdang.com/schema/ddframe/job"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.dangdang.com/schema/ddframe/reg
                        http://www.dangdang.com/schema/ddframe/reg/reg.xsd
                        http://www.dangdang.com/schema/ddframe/job
                        http://www.dangdang.com/schema/ddframe/job/job.xsd
                        ">
    <!--配置作业注册中心 -->
    <reg:zookeeper id="regCenter" server-lists=" yourhost:2181" namespace="dd-job" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />

    <!-- 配置作业-->
    <job:simple id="oneOffElasticJob" monitor-port="9888" class="xxx.MyElasticJob" registry-center-ref="regCenter" cron="0/10 * * * * ?"   sharding-total-count="3" sharding-item-parameters="0=A,1=B,2=C" />
</beans>
```

* Java方式

```java
public class JobMain {
    public static void main(final String[] args) {
        // ...
        jobConfig.setMonitorPort(9888);
        // ...
    }
}
```

### 启动作业

### 执行dump命令

dump命令完全参照Zookeeper的四字命令的理念

```bash
echo "dump" | nc <任意一台作业服务器IP> 9888
```

![dump命令](../../../img/1.x/dump.jpg)

导出到文件

```bash
echo "dump" | nc <任意一台作业服务器IP> 9888 > job_debug.txt
```

## 使用注意事项

务必更新到`1.0.3`以上版本