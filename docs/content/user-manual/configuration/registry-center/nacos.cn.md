+++
title = "Nacos 配置"
weight = 3
+++

Nacos 是一个动态服务发现、配置和服务管理平台，ElasticJob 支持使用 Nacos 3.x 作为注册中心。

每个 ElasticJob 键（如 `/my-job/config`）对应一条 Nacos 配置，ElasticJob 的 namespace 映射为 Nacos 配置的 group。
由于 Nacos `dataId` 只允许字母、数字和 `_-.:`，每个路径段会用 Base64URL 编码后以 `.` 连接。

> 需要 `com.alibaba.nacos:nacos-client:3.2.4`（下面的构件会自动引入）。

## Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere.elasticjob</groupId>
    <artifactId>elasticjob-registry-center-nacos</artifactId>
    <version>${elasticjob.version}</version>
</dependency>
```

## 配置项

### 可配置属性

| 属性名            | 类型     | 缺省值   | 描述                                  |
|----------------|:-------|:------|:------------------------------------|
| serverLists    | String |       | Nacos 服务器地址列表                     |
| namespace      | String |       | ElasticJob 命名空间，映射为 Nacos 配置 group |
| nacosNamespace | String |       | Nacos 租户（命名空间 ID），为空表示 `public`    |
| username       | String |       | 认证用户名                               |
| password       | String |       | 认证密码                                |
| timeoutMs      | long   | 3000  | 配置操作超时毫秒数                          |

### 核心配置项说明

**serverLists:**

包括 IP 地址和端口号，多个地址用逗号分隔，如：`127.0.0.1:8848,127.0.0.2:8848`。
`nacos://` 前缀可选，会被自动去除，因此 `nacos://127.0.0.1:8848` 与 `127.0.0.1:8848` 均可。
通过 `RegistryCenterFactory`（SPI）创建时必须带 `nacos://` 前缀，以便选中 Nacos 实现：
`RegistryCenterFactory.createCoordinatorRegistryCenter("nacos://127.0.0.1:8848", "elasticjob", null)`。

**namespace:**

ElasticJob 命名空间，映射为 Nacos 配置 group，用于隔离不同的作业集群。建议使用有意义的名称，如：`elasticjob`。

**username 和 password:**

Nacos 认证信息，也可以作为 `RegistryCenterFactory` 的 `digest` 参数（`username:password`）传入。

**注意事项与限制：**

- 临时节点为模拟实现：行为与持久配置相同，当前注册中心实例关闭时会被删除。
- 选主为 JVM 本地锁，多节点部署下仅保证每个 JVM 内同一时间只有一个线程执行回调。

## 使用示例

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
        
        // 使用注册中心...
        
        registryCenter.close();
    }
}
```

### RegistryCenterFactory（SPI）

```java
import org.apache.shardingsphere.elasticjob.lifecycle.internal.reg.RegistryCenterFactory;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

public class NacosFactoryExample {
    
    public static void main(String[] args) {
        CoordinatorRegistryCenter registryCenter = RegistryCenterFactory.createCoordinatorRegistryCenter("nacos://127.0.0.1:8848", "elasticjob", null);
        
        // 使用注册中心...
    }
}
```
