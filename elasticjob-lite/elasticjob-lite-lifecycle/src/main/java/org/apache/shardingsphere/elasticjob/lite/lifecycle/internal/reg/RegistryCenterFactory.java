/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.reg;

import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry center factory.
 */
@SuppressWarnings("UnstableApiUsage")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegistryCenterFactory {
    
    private static final Map<HashCode, CoordinatorRegistryCenter> REG_CENTER_REGISTRY = new ConcurrentHashMap<>();
    
    /**
     * Create a {@link CoordinatorRegistryCenter} or return the existing one if there is one set up with the same {@code connectionString}, {@code namespace} and {@code digest} already.
     *
     * @param connectString registry center connect string
     * @param namespace registry center namespace
     * @param digest registry center digest
     * @return registry center
     */
    public static CoordinatorRegistryCenter createCoordinatorRegistryCenter(final String connectString, final String namespace, final String digest) {
        Hasher hasher = Hashing.sha256().newHasher().putString(connectString, StandardCharsets.UTF_8).putString(namespace, StandardCharsets.UTF_8);
        if (!Strings.isNullOrEmpty(digest)) {
            hasher.putString(digest, StandardCharsets.UTF_8);
        }
        HashCode hashCode = hasher.hash();
        return REG_CENTER_REGISTRY.computeIfAbsent(hashCode, unused -> {
            CoordinatorRegistryCenter result = newCoordinatorRegistryCenter(connectString, namespace, digest);
            result.init();
            return result;
        });
    }
    
    private static CoordinatorRegistryCenter newCoordinatorRegistryCenter(final String connectString,
                                                                          final String namespace,
                                                                          final String digest) {
        final ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(connectString, namespace);
        if (!Strings.isNullOrEmpty(digest)) {
            zkConfig.setDigest(digest);
        }
        return new ZookeeperRegistryCenter(zkConfig);
    }
}
