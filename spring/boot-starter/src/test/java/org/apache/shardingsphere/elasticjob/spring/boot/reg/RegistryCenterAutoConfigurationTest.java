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
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

class RegistryCenterAutoConfigurationTest {
    
    @Test
    void assertDefaultZookeeperRegistryCenter() {
        lazyRegistryCenterAutoConfiguration()
                .withPropertyValues("elasticjob.reg-center.namespace=test")
                .run(context -> assertThat(context.containsBeanDefinition("zookeeperRegistryCenter"), is(true)));
    }
    
    @Test
    void assertExplicitZookeeperRegistryCenter() {
        lazyRegistryCenterAutoConfiguration()
                .withPropertyValues("elasticjob.reg-center.type=zookeeper", "elasticjob.reg-center.namespace=test")
                .run(context -> assertThat(context.containsBeanDefinition("zookeeperRegistryCenter"), is(true)));
    }
    
    @Test
    void assertLegacyPropertyNaming() {
        lazyRegistryCenterAutoConfiguration()
                .withPropertyValues("elasticjob.regCenter.type=zookeeper", "elasticjob.regCenter.namespace=test")
                .run(context -> assertThat(context.containsBeanDefinition("zookeeperRegistryCenter"), is(true)));
    }
    
    @Test
    void assertUserDefinedRegistryCenterTakesPrecedence() {
        lazyRegistryCenterAutoConfiguration()
                .withUserConfiguration(UserDefinedRegistryCenterConfiguration.class)
                .withPropertyValues("elasticjob.reg-center.namespace=test")
                .run(context -> {
                    assertThat(context.containsBeanDefinition("zookeeperRegistryCenter"), is(false));
                    assertSame(context.getBean("userDefinedRegistryCenter", CoordinatorRegistryCenter.class), context.getBean(CoordinatorRegistryCenter.class));
                });
    }
    
    private static ApplicationContextRunner lazyRegistryCenterAutoConfiguration() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ElasticJobRegistryCenterConfiguration.class))
                .withInitializer(context -> context.addBeanFactoryPostProcessor(beanFactory -> {
                    if (beanFactory.containsBeanDefinition("zookeeperRegistryCenter")) {
                        beanFactory.getBeanDefinition("zookeeperRegistryCenter").setLazyInit(true);
                    }
                }));
    }
    
    @Configuration(proxyBeanMethods = false)
    static class UserDefinedRegistryCenterConfiguration {
        
        @Bean
        CoordinatorRegistryCenter userDefinedRegistryCenter() {
            return new ZookeeperRegistryCenter(new ZookeeperConfiguration("localhost:2181", "user-defined"));
        }
    }
}
