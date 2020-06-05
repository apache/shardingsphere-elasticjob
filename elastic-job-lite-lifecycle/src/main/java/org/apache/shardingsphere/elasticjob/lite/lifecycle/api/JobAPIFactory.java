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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.api;

import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate.ShardingOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.reg.RegistryCenterFactory;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.settings.JobSettingsAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.statistics.JobStatisticsAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.statistics.ServerStatisticsAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.statistics.ShardingStatisticsAPIImpl;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate.ShardingOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.reg.RegistryCenterFactory;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.settings.JobSettingsAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.statistics.JobStatisticsAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.statistics.ServerStatisticsAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.statistics.ShardingStatisticsAPIImpl;

/**
 * Job API factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobAPIFactory {
    
    /**
     * Create job settings API.
     *
     * @param connectString registry center connect string
     * @param namespace registry center namespace
     * @param digest registry center digest
     * @return job settings API
     */
    public static JobSettingsAPI createJobSettingsAPI(final String connectString, final String namespace, final Optional<String> digest) {
        return new JobSettingsAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter(connectString, namespace, digest));
    }
    
    /**
     * Create job operate API.
     *
     * @param connectString registry center connect string
     * @param namespace registry center namespace
     * @param digest registry center digest
     * @return job operate API
     */
    public static JobOperateAPI createJobOperateAPI(final String connectString, final String namespace, final Optional<String> digest) {
        return new JobOperateAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter(connectString, namespace, digest));
    }
    
    /**
     * Create job sharding operate API.
     *
     * @param connectString registry center connect string
     * @param namespace registry center namespace
     * @param digest registry center digest
     * @return job sharding operate API
     */
    public static ShardingOperateAPI createShardingOperateAPI(final String connectString, final String namespace, final Optional<String> digest) {
        return new ShardingOperateAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter(connectString, namespace, digest));
    }
    
    /**
     * Create job statistics API.
     *
     * @param connectString registry center connect string
     * @param namespace registry center namespace
     * @param digest registry center digest
     * @return job statistics API
     */
    public static JobStatisticsAPI createJobStatisticsAPI(final String connectString, final String namespace, final Optional<String> digest) {
        return new JobStatisticsAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter(connectString, namespace, digest));
    }
    
    /**
     * Create server statistics API.
     *
     * @param connectString registry center connect string
     * @param namespace registry center namespace
     * @param digest registry center digest
     * @return job server statistics API
     */
    public static ServerStatisticsAPI createServerStatisticsAPI(final String connectString, final String namespace, final Optional<String> digest) {
        return new ServerStatisticsAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter(connectString, namespace, digest));
    }
    
    /**
     * Create sharding statistics API.
     *
     * @param connectString registry center connect string
     * @param namespace registry center namespace
     * @param digest registry center digest
     * @return job sharding statistics API
     */
    public static ShardingStatisticsAPI createShardingStatisticsAPI(final String connectString, final String namespace, final Optional<String> digest) {
        return new ShardingStatisticsAPIImpl(RegistryCenterFactory.createCoordinatorRegistryCenter(connectString, namespace, digest));
    }
}
