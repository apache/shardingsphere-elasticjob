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

package org.apache.shardingsphere.elasticjob.lite.spring.boot.reg;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.junit.Test;

public final class ZookeeperPropertiesTest {

    @Test
    public void assertToZookeeperConfiguration() {
        ZookeeperProperties properties = new ZookeeperProperties();
        properties.setServerLists("localhost:18181");
        properties.setNamespace("test");
        properties.setBaseSleepTimeMilliseconds(2000);
        properties.setMaxSleepTimeMilliseconds(4000);
        properties.setMaxRetries(5);
        properties.setSessionTimeoutMilliseconds(5000);
        properties.setConnectionTimeoutMilliseconds(6000);
        properties.setDigest("digest");
        ZookeeperConfiguration actual = properties.toZookeeperConfiguration();
        assertThat(actual.getServerLists(), is(properties.getServerLists()));
        assertThat(actual.getNamespace(), is(properties.getNamespace()));
        assertThat(actual.getBaseSleepTimeMilliseconds(), is(properties.getBaseSleepTimeMilliseconds()));
        assertThat(actual.getMaxSleepTimeMilliseconds(), is(properties.getMaxSleepTimeMilliseconds()));
        assertThat(actual.getMaxRetries(), is(properties.getMaxRetries()));
        assertThat(actual.getSessionTimeoutMilliseconds(), is(properties.getSessionTimeoutMilliseconds()));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(properties.getConnectionTimeoutMilliseconds()));
        assertThat(actual.getDigest(), is(properties.getDigest()));
    }
}
