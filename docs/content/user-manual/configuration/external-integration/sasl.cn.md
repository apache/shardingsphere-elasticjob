+++
title = "连接至开启 SASL 鉴权的 Zookeeper Server"
weight = 2
+++

## 使用方式

ElasticJob 的 `org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter` 能正常连接至开启 SASL 鉴权的 Zookeeper Server。
SASL 机制允许在客户端和服务器之间实现安全通信，而 ZooKeeper 支持 Kerberos 或 DIGEST-MD5 作为身份验证方案。
下文讨论常见情景。

### DIGEST-MD5

假设通过 Docker Engine 部署单个 Zookeeper Server 实例，对应的 `docker-compose.yml` 内容如下，

```yaml
services:
  zookeeper-test:
    image: zookeeper:3.9.2
    volumes:
      - ./jaas-server-test.conf:/jaas-test.conf
    environment:
      JVMFLAGS: "-Djava.security.auth.login.config=/jaas-test.conf"
      ZOO_CFG_EXTRA: "authProvider.1=org.apache.zookeeper.server.auth.SASLAuthenticationProvider sessionRequireClientSASLAuth=true"
    ports:
      - "2181:2181"
```

假设存在文件为 `./jaas-server-test.conf`，内容如下，

```
Server {
    org.apache.zookeeper.server.auth.DigestLoginModule required
    user_bob="bobsecret";
};
```

假设存在独立的 Spring Boot 应用，只需要在 Spring Boot 的启动类配置 SASL 的鉴权信息。逻辑类似如下，

```java
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.util.HashMap;
import java.util.Map;

public class ExampleUtils {
    public void initSasl() {
        Configuration configuration = new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
                Map<String, String> conf = new HashMap<>();
                conf.put("username", "bob");
                conf.put("password", "bobsecret");
                AppConfigurationEntry[] entries = new AppConfigurationEntry[1];
                entries[0] = new AppConfigurationEntry(
                        "org.apache.zookeeper.server.auth.DigestLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        conf);
                return entries;
            }
        };
        Configuration.setConfiguration(configuration);
    }
}
```

此时可正常初始化 ElasticJob 的 `org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter`。逻辑类似如下，

```java
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;

public class ExampleUtils {
    public CoordinatorRegistryCenter initElasticJob() {
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration("127.0.0.1:2181", "test-namespace");
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        regCenter.init();
        return regCenter;
    }
}
```

对于单个 JVM 进程，同一时间只能存在单个 SASL 鉴权信息，因为 Zookeeper Client 通过 JAAS 机制读取 SASL 鉴权信息。
若当前 Spring Boot 应用需切换到使用不同 SASL 鉴权信息的 Zookeeper Server，则需要注销已有的 SASL 鉴权信息。逻辑类似如下，

```java
import javax.security.auth.login.Configuration;

public class ExampleUtils {
    public void exitSasl() {
        Configuration.setConfiguration(null);
    }
}
```

### Kerberos

要使 ElasticJob 的 `org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter` 连接至开启 Kerberos 鉴权的 Zookeeper Server，
流程类似于 DIGEST-MD5。以 https://cwiki.apache.org/confluence/display/ZOOKEEPER/Client-Server+mutual+authentication 为准。

Kerberos KDC 不存在可用的 Docker Image，用户可能需要手动启动 Kerberos KDC。
