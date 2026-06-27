+++
pre = "<b>4.1.2.1 </b>"
title = "Registry Center Configuration"
weight = 1
chapter = true
+++

ElasticJob supports multiple types of registry centers for coordinating the scheduling and execution of distributed jobs.

This section describes how to configure different types of registry centers.

## Supported Registry Center Types

| Registry Center Type | Description |
|---------------------|-------------|
| [ZooKeeper](/en/user-manual/configuration/registry-center/zookeeper) | Apache ZooKeeper, distributed coordination service |
| [etcd](/en/user-manual/configuration/registry-center/etcd) | etcd3, distributed key-value store |

## Configuration Methods

ElasticJob provides the following methods to configure registry centers:

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
    type: zookeeper  # or etcd
    server-lists: host1:2181,host2:2181  # or http://host1:2379,http://host2:2379
    namespace: elasticjob
```

### Spring Namespace

```xml
<!-- ZooKeeper -->
<elasticjob:zookeeper id="regCenter" server-lists="host1:2181,host2:2181" namespace="elasticjob" />

<!-- etcd -->
<elasticjob:etcd id="regCenter" server-lists="http://host1:2379,http://host2:2379" namespace="elasticjob" />
```
