+++
title = "Connect to Zookeeper Server with SASL authentication enabled"
weight = 2
+++

## Usage

ElasticJob's `org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter` can connect to Zookeeper Server with SASL authentication enabled.
The SASL mechanism allows secure communication between the client and the server, 
and ZooKeeper supports Kerberos or DIGEST-MD5 as authentication schemes.
Common scenarios are discussed below.

### DIGEST-MD5

Assuming that a single Zookeeper Server instance is deployed through Docker Engine, 
the corresponding `docker-compose.yml` content is as follows,

```yaml
services:
  zookeeper-test:
    image: zookeeper:3.9.2
    volumes:
      - ./jaas-server-test.conf:/jaas-test.conf
    environment:
      JVMFLAGS: "-Djava.security.auth.login.config=/jaas-test.conf"
      ZOO_CFG_EXTRA: "org.apache.zookeeper.server.auth.SASLAuthenticationProvider sessionRequireClientSASLAuth=true"
    ports:
      - "2181:2181"
```

Assume that there is a file called `./jaas-server-test.conf` with the following content:

```
Server {
    org.apache.zookeeper.server.auth.DigestLoginModule required
    user_bob="bobsecret";
};
```

Assuming there is an independent Spring Boot application, 
users only need to configure SASL authentication information in the Spring Boot startup class. 
The logic is similar to the following:

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
                Map<String, String> options = new HashMap<>();
                options.put("username", "bob");
                options.put("password", "bobsecret");
                AppConfigurationEntry entry = new AppConfigurationEntry(
                        "org.apache.zookeeper.server.auth.DigestLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        options);
                AppConfigurationEntry[] array = new AppConfigurationEntry[1];
                array[0] = entry;
                return array;
            }
        };
        Configuration.setConfiguration(configuration);
    }
}
```

At this time, the `org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter` of ElasticJob can be initialized normally. 
The logic is similar to the following:

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

For a single JVM process, only one SASL authentication information can exist at the same time, 
because Zookeeper Client reads SASL authentication information through the JAAS mechanism.
If the current Spring Boot application needs to switch to a Zookeeper Server that uses different SASL authentication information, 
the existing SASL authentication information needs to be deregistered. 
The logic is similar to the following,

```java
import javax.security.auth.login.Configuration;

public class ExampleUtils {
    public void exitSasl() {
        Configuration.setConfiguration(null);
    }
}
```

### Kerberos

To connect ElasticJob's `org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter` to Zookeeper Server with Kerberos authentication enabled,
the process is similar to DIGEST-MD5. 
Refer to https://cwiki.apache.org/confluence/display/ZOOKEEPER/Client-Server+mutual+authentication .

Some regions may not allow the use of MIT Kerberos source code or binary products. 
Please refer to the MIT Kerberos distribution site https://web.mit.edu/kerberos/dist/index.html .
