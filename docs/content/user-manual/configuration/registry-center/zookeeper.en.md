+++
title = "ZooKeeper Configuration"
weight = 1
+++

Apache ZooKeeper is the default and most mature registry center implementation for ElasticJob.

## Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-registry-center-zookeeper-curator</artifactId>
    <version>${elasticjob.version}</version>
</dependency>
```

## Configuration

### Configuration Properties

| Name                          | Data Type | Default Value | Description                                              |
|-------------------------------|:----------|:--------------|:---------------------------------------------------------|
| serverLists                   | String    |               | ZooKeeper server IP list                                 |
| namespace                     | String    |               | ZooKeeper namespace                                      |
| baseSleepTimeMilliseconds     | int       | 1000          | The initial value of milliseconds for the retry interval |
| maxSleepTimeMilliseconds      | String    | 3000          | The maximum value of milliseconds for the retry interval |
| maxRetries                    | String    | 3             | Maximum number of retries                                |
| sessionTimeoutMilliseconds    | int       | 60000         | Session timeout in milliseconds                          |
| connectionTimeoutMilliseconds | int       | 15000         | Connection timeout in milliseconds                       |
| digest                        | String    | no need       | Permission token to connect to ZooKeeper                 |
| ensembleTracker               | boolean   | true          | Whether to watch ensemble configuration changes          |

### Core Configuration Description

**serverLists:**

Include IP and port, multiple addresses are separated by commas, such as: `host1:2181,host2:2181`

**namespace:**

ZooKeeper namespace for isolating different job clusters. It is recommended to use meaningful names, such as: `elasticjob`

**digest:**

ZooKeeper authentication information, format is `username:password`. If ZooKeeper has SASL authentication enabled, you can use this configuration.

**ensembleTracker:**

Allows configuring whether to watch ensemble configuration changes. This configuration is used to solve the problem when connecting to ZooKeeper clusters via Virtual IPs or load balancers.

In environments like Kubernetes, Pods may restart at any time and Pod IPs will change. If using HA enabled Zookeeper clusters, the Zookeeper Client may return unresolvable URLs during Ensemble Tracking, causing connection failures.
Setting this configuration to `false` allows Curator to always connect to ZooKeeper via the service cluster IP.

> Reference: [Issue #2072](https://github.com/apache/shardingsphere-elasticjob/issues/2072) - Build CuratorFrameworkFactory supports an option to skip Ensemble tracking

## Usage Examples

### Java API

```java
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

public class ZookeeperRegistryCenterExample {
    
    public static void main(String[] args) {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("host1:2181,host2:2181", "elasticjob");
        zkConfig.setBaseSleepTimeMilliseconds(1000);
        zkConfig.setMaxSleepTimeMilliseconds(3000);
        zkConfig.setMaxRetries(3);
        zkConfig.setSessionTimeoutMilliseconds(60000);
        zkConfig.setConnectionTimeoutMilliseconds(15000);
        zkConfig.setDigest("username:password");
        zkConfig.setEnsembleTracker(true);
        
        CoordinatorRegistryCenter registryCenter = new ZookeeperRegistryCenter(zkConfig);
        registryCenter.init();
        
        // Use registry center...
        
        registryCenter.close();
    }
}
```

### Spring Boot Starter

```yaml
elasticjob:
  reg-center:
    type: zookeeper
    server-lists: host1:2181,host2:2181
    namespace: elasticjob
    base-sleep-time-milliseconds: 1000
    max-sleep-time-milliseconds: 3000
    max-retries: 3
    session-timeout-milliseconds: 60000
    connection-timeout-milliseconds: 15000
    digest: username:password
    ensemble-tracker: true
```

### Spring Namespace

```xml
<elasticjob:zookeeper id="regCenter" 
    server-lists="host1:2181,host2:2181" 
    namespace="elasticjob"
    base-sleep-time-milliseconds="1000"
    max-sleep-time-milliseconds="3000"
    max-retries="3"
    session-timeout-milliseconds="60000"
    connection-timeout-milliseconds="15000"
    digest="username:password"
    ensemble-tracker="true" />
```
