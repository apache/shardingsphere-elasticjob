+++
title = "etcd 配置"
weight = 2
+++

etcd 是一个分布式键值存储系统，ElasticJob 支持使用 etcd3 作为注册中心。

## Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-registry-center-etcd</artifactId>
    <version>${elasticjob.version}</version>
</dependency>
```

## 配置项

### 可配置属性

| 属性名                           | 类型     | 缺省值   | 描述                  |
|-------------------------------|:-------|:------|:--------------------|
| serverLists                   | String |       | 连接 etcd 服务器的列表 |
| namespace                     | String |       | etcd 的命名空间     |
| connectionTimeoutMilliseconds | long   | 5000  | 连接超时毫秒数             |
| username                      | String |       | 认证用户名  |
| password                      | String |       | 认证密码  |
| authority                     | String |       | HTTP/2 的 authority 头  |

### 核心配置项说明

**serverLists:**

包括 IP 地址和端口号，多个地址用逗号分隔，需要包含协议前缀，如: `http://host1:2379,http://host2:2379`

**namespace:**

etcd 的命名空间，用于隔离不同的作业集群。建议使用有意义的名称，如: `elasticjob`

**username 和 password:**

etcd 的认证信息。如果 etcd 启用了认证，需要配置用户名和密码。

**authority:**

HTTP/2 的 authority 头，用于某些特殊的网络环境配置。

## 使用示例

### Java API

```java
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdConfiguration;
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

public class EtcdRegistryCenterExample {
    
    public static void main(String[] args) {
        EtcdConfiguration etcdConfig = new EtcdConfiguration("http://host1:2379,http://host2:2379", "elasticjob");
        etcdConfig.setConnectionTimeoutMilliseconds(5000);
        etcdConfig.setUsername("root");
        etcdConfig.setPassword("password");
        
        CoordinatorRegistryCenter registryCenter = new EtcdRegistryCenter(etcdConfig);
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
    type: etcd
    server-lists: http://host1:2379,http://host2:2379
    namespace: elasticjob
    connection-timeout-milliseconds: 5000
    username: root
    password: password
```

### Spring Namespace

```xml
<elasticjob:etcd id="regCenter" 
    server-lists="http://host1:2379,http://host2:2379" 
    namespace="elasticjob"
    connection-timeout-milliseconds="5000"
    username="root"
    password="password" />
```
