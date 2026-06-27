+++
title = "ZooKeeper 配置"
weight = 1
+++

Apache ZooKeeper 是 ElasticJob 默认且最成熟的注册中心实现。

## Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-registry-center-zookeeper-curator</artifactId>
    <version>${elasticjob.version}</version>
</dependency>
```

## 配置项

### 可配置属性

| 属性名                           | 类型     | 缺省值   | 描述                  |
|-------------------------------|:-------|:------|:--------------------|
| serverLists                   | String |       | 连接 ZooKeeper 服务器的列表 |
| namespace                     | String |       | ZooKeeper 的命名空间     |
| baseSleepTimeMilliseconds     | int    | 1000  | 等待重试的间隔时间的初始毫秒数     |
| maxSleepTimeMilliseconds      | String | 3000  | 等待重试的间隔时间的最大毫秒数     |
| maxRetries                    | String | 3     | 最大重试次数              |
| sessionTimeoutMilliseconds    | int    | 60000 | 会话超时毫秒数             |
| connectionTimeoutMilliseconds | int    | 15000 | 连接超时毫秒数             |
| digest                        | String | 无需验证  | 连接 ZooKeeper 的权限令牌  |
| ensembleTracker               | boolean | true | 是否监听集群配置变化 |

### 核心配置项说明

**serverLists:**

包括 IP 地址和端口号，多个地址用逗号分隔，如: `host1:2181,host2:2181`

**namespace:**

ZooKeeper 的命名空间，用于隔离不同的作业集群。建议使用有意义的名称，如: `elasticjob`

**digest:**

ZooKeeper 的权限认证信息，格式为 `username:password`。如果 ZooKeeper 启用了 SASL 认证，可以使用此配置项。

**ensembleTracker:**

允许配置是否监听集群配置变化。此配置项用于解决通过虚拟 IP 或负载均衡器连接 ZooKeeper 集群时的问题。

在 Kubernetes 等环境中，Pod 可能随时重启，Pod IP 会变化。如果使用 HA 启用的 ZooKeeper 集群，ZooKeeper 客户端可能在 Ensemble Tracking 期间返回无法解析的 URL，导致连接失败。
将此配置设置为 `false` 可以让 Curator 始终通过服务集群 IP 连接 ZooKeeper。

> 参考: [Issue #2072](https://github.com/apache/shardingsphere-elasticjob/issues/2072) - Build CuratorFrameworkFactory supports an option to skip Ensemble tracking

## 使用示例

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
        
        // 使用注册中心...
        
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
