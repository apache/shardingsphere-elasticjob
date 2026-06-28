+++
pre = "<b>4.1.2.1 </b>"
title = "注册中心配置"
weight = 1
chapter = true
+++

ElasticJob 支持多种注册中心类型，用于协调分布式作业的调度和执行。

本章节介绍如何配置不同类型的注册中心。

## 支持的注册中心类型

| 注册中心类型 | 说明 |
|-------------|------|
| [ZooKeeper](/cn/user-manual/configuration/registry-center/zookeeper) | Apache ZooKeeper，分布式协调服务 |
| [etcd](/cn/user-manual/configuration/registry-center/etcd) | etcd3，分布式键值存储 |

## 配置方式

ElasticJob 提供了以下配置注册中心的方式：

### Java API

```java
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

public class ZookeeperExample {
    
    public static void main(String[] args) {
        // ZooKeeper
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("host1:2181,host2:2181", "elasticjob");
        CoordinatorRegistryCenter registryCenter = new ZookeeperRegistryCenter(zkConfig);
        registryCenter.init();
    }
}
```

```java
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdConfiguration;
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

public class EtcdExample {
    
    public static void main(String[] args) {
        // etcd
        EtcdConfiguration etcdConfig = new EtcdConfiguration("http://host1:2379,http://host2:2379", "elasticjob");
        CoordinatorRegistryCenter registryCenter = new EtcdRegistryCenter(etcdConfig);
        registryCenter.init();
    }
}
```

### Spring Boot Starter

```yaml
elasticjob:
  reg-center:
    type: zookeeper  # 或 etcd
    server-lists: host1:2181,host2:2181  # 或 http://host1:2379,http://host2:2379
    namespace: elasticjob
```

### Spring Namespace

```xml
<!-- ZooKeeper -->
<elasticjob:zookeeper id="regCenter" server-lists="host1:2181,host2:2181" namespace="elasticjob" />

<!-- etcd -->
<elasticjob:etcd id="regCenter" server-lists="http://host1:2379,http://host2:2379" namespace="elasticjob" />
```
