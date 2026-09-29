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

package org.apache.shardingsphere.elasticjob.spring.namespace.reg;

import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.spring.namespace.EmbedTestingServerInitializer;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:META-INF/reg/regContext.xml", initializers = EmbedTestingServerInitializer.class)
class ZookeeperRegistryCenterNamespaceTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void assertZookeeperRegistryCenterWithDefaultValues() {
        ZookeeperRegistryCenter regCenter = applicationContext.getBean("regCenter1", ZookeeperRegistryCenter.class);
        assertInstanceOf(ZookeeperRegistryCenter.class, regCenter);
        ZookeeperConfiguration actual = getZookeeperConfiguration(regCenter);
        assertThat(actual.getNamespace(), is("regCenter1"));
        assertThat(actual.getBaseSleepTimeMilliseconds(), is(1000));
        assertThat(actual.getMaxSleepTimeMilliseconds(), is(3000));
        assertThat(actual.getMaxRetries(), is(3));
        assertThat(actual.getSessionTimeoutMilliseconds(), is(0));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(0));
        assertThat(actual.getDigest(), is(nullValue()));
        assertThat(actual.isEnsembleTracker(), is(true));
    }
    
    @Test
    void assertZookeeperRegistryCenterWithCustomValues() {
        ZookeeperRegistryCenter regCenter = applicationContext.getBean("regCenter2", ZookeeperRegistryCenter.class);
        assertInstanceOf(ZookeeperRegistryCenter.class, regCenter);
        ZookeeperConfiguration actual = getZookeeperConfiguration(regCenter);
        assertThat(actual.getNamespace(), is("regCenter2"));
        assertThat(actual.getSessionTimeoutMilliseconds(), is(1000));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(1000));
        assertThat(actual.getDigest(), is("test:digest"));
        assertThat(actual.isEnsembleTracker(), is(false));
    }
    
    @Test
    void assertZookeeperRegistryCenterReadWrite() {
        ZookeeperRegistryCenter regCenter = applicationContext.getBean("regCenter1", ZookeeperRegistryCenter.class);
        regCenter.persist("/test", "value");
        assertThat(regCenter.get("/test"), is("value"));
    }
    
    private ZookeeperConfiguration getZookeeperConfiguration(final ZookeeperRegistryCenter regCenter) {
        return (ZookeeperConfiguration) ReflectionUtils.getFieldValue(regCenter, "zkConfig");
    }
}
