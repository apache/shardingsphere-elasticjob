+++
title = "Nacos Configuration"
weight = 3
+++

Nacos is a dynamic service discovery, configuration and service management platform. ElasticJob supports using Nacos 3.x as a registry center.

Each ElasticJob key (for example `/my-job/config`) is stored as one Nacos config. The ElasticJob namespace is mapped to the Nacos config group.
Because a Nacos `dataId` only accepts letters, digits and `_-.:`, every path segment is encoded with Base64URL and joined with `.`.

> Requires `com.alibaba.nacos:nacos-client:3.2.4` (pulled in automatically by the artifact below).

## Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-registry-center-nacos</artifactId>
    <version>${elasticjob.version}</version>
</dependency>
```

## Configuration

### Configuration Properties

| Name            | Data Type | Default Value  | Description                                              |
|-----------------|:----------|:---------------|:---------------------------------------------------------|
| serverLists     | String    |                | Nacos server address list                                |
| namespace       | String    |                | ElasticJob namespace, mapped to the Nacos config group   |
| nacosNamespace  | String    |                | Nacos tenant (namespace id), empty means `public`        |
| username        | String    |                | Authentication username                                  |
| password        | String    |                | Authentication password                                  |
| timeoutMs       | long      | 3000           | Timeout of config operations in milliseconds             |

### Core Configuration Description

**serverLists:**

Include IP and port, multiple addresses are separated by commas, such as: `127.0.0.1:8848,127.0.0.2:8848`.
The `nacos://` prefix is optional and will be stripped automatically, so both `nacos://127.0.0.1:8848` and `127.0.0.1:8848` work.
When using `RegistryCenterFactory` (SPI), the `nacos://` prefix is required so the Nacos creator can be selected:
`RegistryCenterFactory.createCoordinatorRegistryCenter("nacos://127.0.0.1:8848", "elasticjob", null)`.

**namespace:**

ElasticJob namespace, mapped to the Nacos config group for isolating different job clusters. It is recommended to use meaningful names, such as: `elasticjob`.

**username and password:**

Nacos authentication information. They can also be passed as the `digest` argument (`username:password`) of `RegistryCenterFactory`.

**Notes and limitations:**

- Ephemeral nodes are emulated: they behave like persistent configs and are removed when the current registry center instance is closed.
- Leader election is JVM-local. In a multi-node deployment only one thread per JVM is guaranteed to execute the callback.

## Usage Examples

### Java API

```java
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosConfiguration;
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

public class NacosRegistryCenterExample {
    
    public static void main(String[] args) {
        NacosConfiguration nacosConfig = new NacosConfiguration("nacos://127.0.0.1:8848", "elasticjob");
        nacosConfig.setUsername("nacos");
        nacosConfig.setPassword("nacos");
        nacosConfig.setTimeoutMs(3000L);
        
        CoordinatorRegistryCenter registryCenter = new NacosRegistryCenter(nacosConfig);
        registryCenter.init();
        
        // Use registry center...
        
        registryCenter.close();
    }
}
```

### RegistryCenterFactory (SPI)

```java
import org.apache.shardingsphere.elasticjob.lifecycle.internal.reg.RegistryCenterFactory;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

public class NacosFactoryExample {
    
    public static void main(String[] args) {
        CoordinatorRegistryCenter registryCenter = RegistryCenterFactory.createCoordinatorRegistryCenter("nacos://127.0.0.1:8848", "elasticjob", null);
        
        // Use registry center...
    }
}
```
