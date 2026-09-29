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
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdConfiguration;
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdRegistryCenter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the etcd registry center.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EtcdRegistryCenter.class)
@ConditionalOnProperty(name = "elasticjob.reg-center.type", havingValue = "etcd")
public class EtcdRegistryCenterConfiguration {
    
    /**
     * Create an etcd registry center bean.
     *
     * @param registryCenterProperties configuration properties of the registry center
     * @return etcd registry center
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    @ConditionalOnMissingBean(CoordinatorRegistryCenter.class)
    public EtcdRegistryCenter etcdRegistryCenter(final RegistryCenterProperties registryCenterProperties) {
        return new EtcdRegistryCenter(toEtcdConfiguration(registryCenterProperties));
    }
    
    static EtcdConfiguration toEtcdConfiguration(final RegistryCenterProperties properties) {
        EtcdConfiguration result = new EtcdConfiguration(properties.getServerLists(), properties.getNamespace());
        if (0 != properties.getConnectionTimeoutMilliseconds()) {
            result.setConnectionTimeoutMilliseconds(properties.getConnectionTimeoutMilliseconds());
        }
        result.setUsername(properties.getUsername());
        result.setPassword(properties.getPassword());
        result.setSsl(properties.isSsl());
        result.setAuthority(properties.getAuthority());
        return result;
    }
}
