+++
title = "etcd Configuration"
weight = 2
+++

etcd is a distributed key-value store system. ElasticJob supports using etcd3 as a registry center.

## Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-registry-center-etcd</artifactId>
    <version>${elasticjob.version}</version>
</dependency>
```

## Configuration

### Configuration Properties

| Name                          | Data Type | Default Value | Description                                              |
|-------------------------------|:----------|:--------------|:---------------------------------------------------------|
| serverLists                   | String    |               | etcd server IP list                                      |
| namespace                     | String    |               | etcd namespace                                           |
| connectionTimeoutMilliseconds | long      | 5000          | Connection timeout in milliseconds                       |
| username                      | String    |               | Authentication username                                  |
| password                      | String    |               | Authentication password                                  |
| authority                     | String    |               | Authority header for HTTP/2                              |

### Core Configuration Description

**serverLists:**

Include IP and port, multiple addresses are separated by commas, protocol prefix is required, such as: `http://host1:2379,http://host2:2379`

**namespace:**

etcd namespace for isolating different job clusters. It is recommended to use meaningful names, such as: `elasticjob`

**username and password:**

etcd authentication information. If etcd has authentication enabled, you need to configure both username and password.

**authority:**

HTTP/2 authority header, used for some special network environment configurations.

## Usage Examples

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
        
        // Use registry center...
        
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
