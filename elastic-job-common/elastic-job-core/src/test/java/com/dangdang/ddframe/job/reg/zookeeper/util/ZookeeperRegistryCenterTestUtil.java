package com.dangdang.ddframe.job.reg.zookeeper.util;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZookeeperRegistryCenterTestUtil {
    
    public static void persist(final ZookeeperRegistryCenter zookeeperRegistryCenter) {
        zookeeperRegistryCenter.persist("/test", "test");
        zookeeperRegistryCenter.persist("/test/deep/nested", "deepNested");
        zookeeperRegistryCenter.persist("/test/child", "child");
    } 
}
