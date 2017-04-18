/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.lifecycle.internal.reg;

import com.dangdang.ddframe.job.lite.lifecycle.AbstractEmbedZookeeperBaseTest;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Optional;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class RegistryCenterFactoryTest extends AbstractEmbedZookeeperBaseTest {
    
    @Test
    public void assertCreateCoordinatorRegistryCenterWithoutDigest() throws ReflectiveOperationException {
        ZookeeperConfiguration zkConfig = getZookeeperConfiguration(RegistryCenterFactory.createCoordinatorRegistryCenter(getConnectionString(), "namespace", Optional.<String>absent()));
        assertThat(zkConfig.getNamespace(), is("namespace"));
        assertNull(zkConfig.getDigest());
    }
    
    @Test
    public void assertCreateCoordinatorRegistryCenterWithDigest() throws ReflectiveOperationException {
        ZookeeperConfiguration zkConfig = getZookeeperConfiguration(RegistryCenterFactory.createCoordinatorRegistryCenter(getConnectionString(), "namespace", Optional.of("digest")));
        assertThat(zkConfig.getNamespace(), is("namespace"));
        assertThat(zkConfig.getDigest(), is("digest"));
    }
    
    @Test
    public void assertCreateCoordinatorRegistryCenterFromCache() throws ReflectiveOperationException {
        RegistryCenterFactory.createCoordinatorRegistryCenter(getConnectionString(), "otherNamespace", Optional.<String>absent());
        ZookeeperConfiguration zkConfig = getZookeeperConfiguration(RegistryCenterFactory.createCoordinatorRegistryCenter(getConnectionString(), "otherNamespace", Optional.<String>absent()));
        assertThat(zkConfig.getNamespace(), is("otherNamespace"));
        assertNull(zkConfig.getDigest());
    }
    
    private ZookeeperConfiguration getZookeeperConfiguration(final CoordinatorRegistryCenter regCenter) throws ReflectiveOperationException {
        Method method = ZookeeperRegistryCenter.class.getDeclaredMethod("getZkConfig");
        method.setAccessible(true);
        return (ZookeeperConfiguration) method.invoke(regCenter);
    }
}
