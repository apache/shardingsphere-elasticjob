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

import org.apache.curator.test.InstanceSpec;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class RegistryCenterPropertiesTest {
    
    @Test
    void assertDefaultValues() {
        RegistryCenterProperties properties = new RegistryCenterProperties();
        assertThat(properties.getType(), is("zookeeper"));
        assertThat(properties.getServerLists(), is(nullValue()));
        assertThat(properties.getNamespace(), is(nullValue()));
        assertThat(properties.getBaseSleepTimeMilliseconds(), is(1000));
        assertThat(properties.getMaxSleepTimeMilliseconds(), is(3000));
        assertThat(properties.getMaxRetries(), is(3));
        assertThat(properties.getSessionTimeoutMilliseconds(), is(0));
        assertThat(properties.getConnectionTimeoutMilliseconds(), is(0));
        assertThat(properties.getDigest(), is(nullValue()));
        assertThat(properties.isEnsembleTracker(), is(true));
    }
    
    @Test
    void assertToZookeeperConfigurationWithDefaultValues() {
        RegistryCenterProperties properties = newRegistryCenterProperties("localhost:" + InstanceSpec.getRandomPort());
        ZookeeperConfiguration actual = ZookeeperRegistryCenterConfiguration.toZookeeperConfiguration(properties);
        assertThat(actual.getServerLists(), is(properties.getServerLists()));
        assertThat(actual.getNamespace(), is(properties.getNamespace()));
        assertThat(actual.getBaseSleepTimeMilliseconds(), is(1000));
        assertThat(actual.getMaxSleepTimeMilliseconds(), is(3000));
        assertThat(actual.getMaxRetries(), is(3));
        assertThat(actual.getSessionTimeoutMilliseconds(), is(0));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(0));
        assertThat(actual.getDigest(), is(nullValue()));
        assertThat(actual.isEnsembleTracker(), is(true));
    }
    
    @Test
    void assertToZookeeperConfiguration() {
        ZookeeperConfiguration actual = ZookeeperRegistryCenterConfiguration.toZookeeperConfiguration(newFullProperties());
        assertThat(actual.getServerLists(), is("host1:2181,host2:2181"));
        assertThat(actual.getNamespace(), is("test"));
        assertThat(actual.getBaseSleepTimeMilliseconds(), is(2000));
        assertThat(actual.getMaxSleepTimeMilliseconds(), is(4000));
        assertThat(actual.getMaxRetries(), is(5));
        assertThat(actual.getSessionTimeoutMilliseconds(), is(5000));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(6000));
        assertThat(actual.getDigest(), is("user:digest"));
        assertThat(actual.isEnsembleTracker(), is(false));
    }
    
    private static RegistryCenterProperties newRegistryCenterProperties(final String serverLists) {
        RegistryCenterProperties properties = new RegistryCenterProperties();
        properties.setServerLists(serverLists);
        properties.setNamespace("test");
        return properties;
    }
    
    private static RegistryCenterProperties newFullProperties() {
        RegistryCenterProperties properties = newRegistryCenterProperties("host1:2181,host2:2181");
        properties.setBaseSleepTimeMilliseconds(2000);
        properties.setMaxSleepTimeMilliseconds(4000);
        properties.setMaxRetries(5);
        properties.setSessionTimeoutMilliseconds(5000);
        properties.setConnectionTimeoutMilliseconds(6000);
        properties.setDigest("user:digest");
        properties.setEnsembleTracker(false);
        return properties;
    }
}
