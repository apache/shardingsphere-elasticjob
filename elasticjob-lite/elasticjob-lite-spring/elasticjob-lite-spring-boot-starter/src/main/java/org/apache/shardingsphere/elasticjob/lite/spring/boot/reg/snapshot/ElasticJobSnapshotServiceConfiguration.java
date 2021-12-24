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

package org.apache.shardingsphere.elasticjob.lite.spring.boot.reg.snapshot;

import org.apache.shardingsphere.elasticjob.lite.internal.snapshot.SnapshotService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Snapshot service configuration.
 */
@ConditionalOnProperty(name = "elasticjob.dump.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SnapshotServiceProperties.class)
public class ElasticJobSnapshotServiceConfiguration {
    
    /**
     * Create a Snapshot service bean and start listening.
     *
     * @param registryCenter registry center
     * @param snapshotServiceProperties snapshot service properties
     * @return a bean of snapshot service
     */
    @ConditionalOnProperty(name = "elasticjob.dump.port")
    @Bean(initMethod = "listen", destroyMethod = "close")
    public SnapshotService snapshotService(final CoordinatorRegistryCenter registryCenter, final SnapshotServiceProperties snapshotServiceProperties) {
        return new SnapshotService(registryCenter, snapshotServiceProperties.getPort());
    }
}
