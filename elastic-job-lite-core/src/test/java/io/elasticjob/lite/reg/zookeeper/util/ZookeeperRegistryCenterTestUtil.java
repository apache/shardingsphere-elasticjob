package io.elasticjob.lite.reg.zookeeper.util;

import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZookeeperRegistryCenterTestUtil {
    
    /**
     * Persist the data to registry center.
     *
     * @param zookeeperRegistryCenter registry center
     */
    public static void persist(final ZookeeperRegistryCenter zookeeperRegistryCenter) {
        zookeeperRegistryCenter.persist("/test", "test");
        zookeeperRegistryCenter.persist("/test/deep/nested", "deepNested");
        zookeeperRegistryCenter.persist("/test/child", "child");
    } 
}
