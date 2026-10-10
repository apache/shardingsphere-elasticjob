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
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosConfiguration;
import org.apache.shardingsphere.elasticjob.reg.nacos.NacosRegistryCenter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the Nacos registry center.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(NacosRegistryCenter.class)
@ConditionalOnProperty(name = "elasticjob.reg-center.type", havingValue = "nacos")
public class NacosRegistryCenterConfiguration {
    
    /**
     * Create a Nacos registry center bean.
     *
     * @param registryCenterProperties configuration properties of the registry center
     * @return Nacos registry center
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    @ConditionalOnMissingBean(CoordinatorRegistryCenter.class)
    public NacosRegistryCenter nacosRegistryCenter(final RegistryCenterProperties registryCenterProperties) {
        return new NacosRegistryCenter(toNacosConfiguration(registryCenterProperties));
    }
    
    static NacosConfiguration toNacosConfiguration(final RegistryCenterProperties properties) {
        NacosConfiguration result = new NacosConfiguration(properties.getServerLists(), properties.getNamespace());
        result.setTenant(properties.getTenant());
        result.setUsername(properties.getUsername());
        result.setPassword(properties.getPassword());
        if (0 != properties.getTimeoutMs()) {
            result.setTimeoutMs(properties.getTimeoutMs());
        }
        return result;
    }
}
