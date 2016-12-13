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

package com.dangdang.ddframe.job.cloud.scheduler.container;

import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.boot.env.FrameworkMode;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.EmbedTestingServer;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.junit.Test;
import org.mockito.Mockito;
import org.unitils.util.ReflectionUtils;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class FrameworkContainerFactoryTest {
    
    private static final String NAME_SPACE = FrameworkContainerFactoryTest.class.getName();
    
    private final BootstrapEnvironment bootstrapEnvironment = BootstrapEnvironment.getInstance();
    
    @Test
    public void assertStandalone() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.HOSTNAME.getKey(), "127.0.0.1");
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        assertThat(AbstractFrameworkContainer.newFrameworkContainer(Mockito.mock(CoordinatorRegistryCenter.class)), instanceOf(StandaloneFrameworkContainer.class));
    
        properties = new Properties();
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.HOSTNAME.getKey(), "127.0.0.1");
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.MODE.getKey(), FrameworkMode.STANDALONE.toString());
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        assertThat(AbstractFrameworkContainer.newFrameworkContainer(Mockito.mock(CoordinatorRegistryCenter.class)), instanceOf(StandaloneFrameworkContainer.class));
    }
    
    @Test
    public void assertHA() throws Exception {
        EmbedTestingServer.start();
        Properties properties = new Properties();
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.HOSTNAME.getKey(), "127.0.0.1");
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.ZOOKEEPER_SERVERS.getKey(), EmbedTestingServer.getConnectionString());
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.MODE.getKey(), FrameworkMode.HA.toString());
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), NAME_SPACE);
        zookeeperConfiguration.setSessionTimeoutMilliseconds(5000);
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(5000);
        ZookeeperRegistryCenter zkRegCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        zkRegCenter.init();
        assertThat(AbstractFrameworkContainer.newFrameworkContainer(zkRegCenter), instanceOf(HAFrameworkContainer.class));
    }
    
}
