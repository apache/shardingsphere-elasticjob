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

package org.apache.shardingsphere.elasticjob.reg.boot;

import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticJobRegistryCenterAutoConfiguration {

    /**
     * Create a ZookeeperConfigurationFactory.
     *
     * @return ZookeeperConfigurationFactory
     */
    @Bean
    @ConfigurationProperties(prefix = "elasticjob.reg-center")
    public ZookeeperConfigurationFactory zookeeperConfigurationFactory() {
        return new ZookeeperConfigurationFactory();
    }

    /**
     * Create a ZookeeperRegistryCenter bean via factory.
     *
     * @param zookeeperConfigurationFactory factory
     * @return ZookeeperRegistryCenter
     */
    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter zookeeperRegistryCenter(final ZookeeperConfigurationFactory zookeeperConfigurationFactory) {
        return new ZookeeperRegistryCenter(zookeeperConfigurationFactory.toZookeeperConfiguration());
    }
}
