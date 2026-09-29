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
import org.apache.shardingsphere.elasticjob.reg.memory.MemoryConfiguration;
import org.apache.shardingsphere.elasticjob.reg.memory.MemoryRegistryCenter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class RegistryCenterAutoConfigurationTest {
    
    private static final List<String> REGISTRY_CENTER_BEAN_NAMES = Arrays.asList("zookeeperRegistryCenter", "etcdRegistryCenter", "nacosRegistryCenter", "memoryRegistryCenter");
    
    @Test
    void assertDefaultZookeeperRegistryCenter() {
        lazyRegistryCenterAutoConfiguration()
                .withPropertyValues("elasticjob.reg-center.namespace=test")
                .run(context -> assertOnlyRegistryCenterBeanDefinition(context, "zookeeperRegistryCenter"));
    }
    
    @Test
    void assertLegacyPropertyNaming() {
        lazyRegistryCenterAutoConfiguration()
                .withPropertyValues("elasticjob.regCenter.type=memory", "elasticjob.regCenter.namespace=test")
                .run(context -> assertOnlyRegistryCenterBeanDefinition(context, "memoryRegistryCenter"));
    }
    
    @Test
    void assertEtcdRegistryCenter() {
        lazyRegistryCenterAutoConfiguration()
                .withPropertyValues("elasticjob.reg-center.type=etcd")
                .run(context -> assertOnlyRegistryCenterBeanDefinition(context, "etcdRegistryCenter"));
    }
    
    @Test
    void assertNacosRegistryCenter() {
        lazyRegistryCenterAutoConfiguration()
                .withPropertyValues("elasticjob.reg-center.type=nacos")
                .run(context -> assertOnlyRegistryCenterBeanDefinition(context, "nacosRegistryCenter"));
    }
    
    @Test
    void assertMemoryRegistryCenter() {
        lazyRegistryCenterAutoConfiguration()
                .withPropertyValues("elasticjob.reg-center.type=memory", "elasticjob.reg-center.namespace=test")
                .run(context -> {
                    assertOnlyRegistryCenterBeanDefinition(context, "memoryRegistryCenter");
                    CoordinatorRegistryCenter registryCenter = context.getBean(CoordinatorRegistryCenter.class);
                    assertInstanceOf(MemoryRegistryCenter.class, registryCenter);
                    registryCenter.persist("/test", "value");
                    assertThat(registryCenter.get("/test"), is("value"));
                });
    }
    
    @Test
    void assertUserDefinedRegistryCenterTakesPrecedence() {
        lazyRegistryCenterAutoConfiguration()
                .withUserConfiguration(UserDefinedRegistryCenterConfiguration.class)
                .withPropertyValues("elasticjob.reg-center.type=memory", "elasticjob.reg-center.namespace=test")
                .run(context -> {
                    assertOnlyRegistryCenterBeanDefinition(context, "memoryRegistryCenter");
                    assertSame(context.getBean(MemoryRegistryCenter.class), context.getBean(CoordinatorRegistryCenter.class));
                });
    }
    
    private static ApplicationContextRunner lazyRegistryCenterAutoConfiguration() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ElasticJobRegistryCenterConfiguration.class))
                .withInitializer(context -> context.addBeanFactoryPostProcessor(beanFactory -> {
                    for (String beanDefinitionName : REGISTRY_CENTER_BEAN_NAMES) {
                        if (beanFactory.containsBeanDefinition(beanDefinitionName)) {
                            beanFactory.getBeanDefinition(beanDefinitionName).setLazyInit(true);
                        }
                    }
                }));
    }
    
    private static void assertOnlyRegistryCenterBeanDefinition(final AssertableApplicationContext context, final String expectedBeanName) {
        List<String> registryCenterBeans = new ArrayList<>();
        for (String beanName : REGISTRY_CENTER_BEAN_NAMES) {
            if (context.containsBeanDefinition(beanName)) {
                registryCenterBeans.add(beanName);
            }
        }
        assertThat(registryCenterBeans, is(Collections.singletonList(expectedBeanName)));
    }
    
    @Configuration(proxyBeanMethods = false)
    static class UserDefinedRegistryCenterConfiguration {
        
        @Bean(initMethod = "init", destroyMethod = "close")
        MemoryRegistryCenter memoryRegistryCenter() {
            return new MemoryRegistryCenter(new MemoryConfiguration("user-defined"));
        }
    }
}
