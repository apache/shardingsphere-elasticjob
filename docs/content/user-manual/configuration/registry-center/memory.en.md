+++
title = "Memory Configuration"
weight = 4
+++

Memory is a built-in in-process registry center implementation. It stores all data in a static `ConcurrentHashMap` and requires no external server.

> Only for unit tests and local development in a single JVM.
> data is neither persisted nor shared across processes, and leader election is JVM-local.

Instances sharing the same namespace share the same in-memory data; different namespaces are isolated from each other.
Ephemeral nodes are removed when the registry center instance that created them is closed.
Unlike etcd (whose leases expire on a timer), memory ephemeral nodes never expire while the creating instance is open.

## Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-registry-center-memory</artifactId>
    <version>${elasticjob.version}</version>
</dependency>
```

## Configuration

### Configuration Properties

| Name        | Data Type | Default Value | Description                              |
|-------------|:----------|:--------------|:-----------------------------------------|
| namespace   | String    |               | Namespace used to isolate in-memory data |

## Usage Examples

### Java API

```java
import org.apache.shardingsphere.elasticjob.reg.memory.MemoryConfiguration;
import org.apache.shardingsphere.elasticjob.reg.memory.MemoryRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

public class MemoryRegistryCenterExample {
    
    public static void main(String[] args) {
        MemoryConfiguration memoryConfig = new MemoryConfiguration("elasticjob");
        
        CoordinatorRegistryCenter registryCenter = new MemoryRegistryCenter(memoryConfig);
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

public class MemoryFactoryExample {
    
    public static void main(String[] args) {
        CoordinatorRegistryCenter registryCenter = RegistryCenterFactory.createCoordinatorRegistryCenter("memory://localhost", "elasticjob", null);
        
        // Use registry center...
    }
}
```
