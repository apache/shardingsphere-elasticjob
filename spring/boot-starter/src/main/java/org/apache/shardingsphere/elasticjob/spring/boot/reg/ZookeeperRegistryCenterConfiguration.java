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

package org.apache.shardingsphere.elasticjob.spring.boot.reg;

import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the ZooKeeper registry center.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ZookeeperRegistryCenter.class)
@ConditionalOnProperty(name = "elasticjob.reg-center.type", havingValue = "zookeeper", matchIfMissing = true)
public class ZookeeperRegistryCenterConfiguration {
    
    /**
     * Create a ZooKeeper registry center bean.
     *
     * @param registryCenterProperties configuration properties of the registry center
     * @return ZooKeeper registry center
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    @ConditionalOnMissingBean(CoordinatorRegistryCenter.class)
    public ZookeeperRegistryCenter zookeeperRegistryCenter(final RegistryCenterProperties registryCenterProperties) {
        return new ZookeeperRegistryCenter(toZookeeperConfiguration(registryCenterProperties));
    }
    
    static ZookeeperConfiguration toZookeeperConfiguration(final RegistryCenterProperties properties) {
        ZookeeperConfiguration result = new ZookeeperConfiguration(properties.getServerLists(), properties.getNamespace());
        result.setBaseSleepTimeMilliseconds(properties.getBaseSleepTimeMilliseconds());
        result.setMaxSleepTimeMilliseconds(properties.getMaxSleepTimeMilliseconds());
        result.setMaxRetries(properties.getMaxRetries());
        result.setSessionTimeoutMilliseconds(properties.getSessionTimeoutMilliseconds());
        result.setConnectionTimeoutMilliseconds(properties.getConnectionTimeoutMilliseconds());
        result.setDigest(properties.getDigest());
        result.setEnsembleTracker(properties.isEnsembleTracker());
        return result;
    }
}
