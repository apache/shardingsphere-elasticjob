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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.reg;

import org.apache.shardingsphere.elasticjob.lite.lifecycle.AbstractEmbedZookeeperBaseTest;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class RegistryCenterFactoryTest extends AbstractEmbedZookeeperBaseTest {
    
    @Test
    public void assertCreateCoordinatorRegistryCenterWithoutDigest() throws ReflectiveOperationException {
        ZookeeperConfiguration zkConfig = getZookeeperConfiguration(RegistryCenterFactory.createCoordinatorRegistryCenter(getConnectionString(), "namespace", null));
        assertThat(zkConfig.getNamespace(), is("namespace"));
        assertNull(zkConfig.getDigest());
    }
    
    @Test
    public void assertCreateCoordinatorRegistryCenterWithDigest() throws ReflectiveOperationException {
        ZookeeperConfiguration zkConfig = getZookeeperConfiguration(RegistryCenterFactory.createCoordinatorRegistryCenter(getConnectionString(), "namespace", "digest"));
        assertThat(zkConfig.getNamespace(), is("namespace"));
        assertThat(zkConfig.getDigest(), is("digest"));
    }
    
    @Test
    public void assertCreateCoordinatorRegistryCenterFromCache() throws ReflectiveOperationException {
        RegistryCenterFactory.createCoordinatorRegistryCenter(getConnectionString(), "otherNamespace", null);
        ZookeeperConfiguration zkConfig = getZookeeperConfiguration(RegistryCenterFactory.createCoordinatorRegistryCenter(getConnectionString(), "otherNamespace", null));
        assertThat(zkConfig.getNamespace(), is("otherNamespace"));
        assertNull(zkConfig.getDigest());
    }
    
    private ZookeeperConfiguration getZookeeperConfiguration(final CoordinatorRegistryCenter regCenter) throws ReflectiveOperationException {
        Method method = ZookeeperRegistryCenter.class.getDeclaredMethod("getZkConfig");
        method.setAccessible(true);
        return (ZookeeperConfiguration) method.invoke(regCenter);
    }
}
