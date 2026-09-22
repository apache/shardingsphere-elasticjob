+++
title = "Memory 配置"
weight = 4
+++

Memory 是内置的进程内注册中心实现，所有数据保存在静态 `ConcurrentHashMap` 中，无需任何外部服务。

> 仅适用于单 JVM 下的单元测试与本地开发。
> 数据不持久化、不跨进程共享，选主也只是 JVM 本地锁。

相同 namespace 的实例共享同一份内存数据，不同 namespace 之间相互隔离。
创建临时节点的实例关闭时，其创建的临时节点会被删除。
与 etcd（租约会定时过期）不同，创建实例存活期间内存临时节点永不过期。

## Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-registry-center-memory</artifactId>
    <version>${elasticjob.version}</version>
</dependency>
```

## 配置项

### 可配置属性

| 属性名       | 类型     | 缺省值 | 描述             |
|-----------|:-------|:----|:---------------|
| namespace | String |     | 用于隔离内存数据的命名空间 |

## 使用示例

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
        
        // 使用注册中心...
        
        registryCenter.close();
    }
}
```

### RegistryCenterFactory（SPI）

```java
import org.apache.shardingsphere.elasticjob.lifecycle.internal.reg.RegistryCenterFactory;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

public class MemoryFactoryExample {
    
    public static void main(String[] args) {
        CoordinatorRegistryCenter registryCenter = RegistryCenterFactory.createCoordinatorRegistryCenter("memory://localhost", "elasticjob", null);
        
        // 使用注册中心...
    }
}
```
