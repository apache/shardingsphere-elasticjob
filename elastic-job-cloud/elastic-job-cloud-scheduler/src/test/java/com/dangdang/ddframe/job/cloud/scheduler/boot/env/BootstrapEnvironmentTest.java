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

package com.dangdang.ddframe.job.cloud.scheduler.boot.env;

import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment.EnvironmentArgument;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.unitils.util.ReflectionUtils;

import java.io.IOException;
import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class BootstrapEnvironmentTest {
    
    private BootstrapEnvironment bootstrapEnvironment;
    
    @Before
    public void setUp() throws IOException {
        bootstrapEnvironment = new BootstrapEnvironment();
    }
    
    @Test
    public void assertGetMesosConfiguration() throws NoSuchFieldException {
        Properties properties = new Properties();
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.HOSTNAME.getKey(), "127.0.0.1");
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        MesosConfiguration mesosConfig = bootstrapEnvironment.getMesosConfiguration();
        assertThat(mesosConfig.getHostname(), is("127.0.0.1"));
        assertThat(mesosConfig.getUser(), is(""));
        assertThat(mesosConfig.getUrl(), is("zk://localhost:2181/mesos"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetMesosConfigurationWithoutRequiredProperties() throws NoSuchFieldException {
        bootstrapEnvironment.getMesosConfiguration();
    }
    
    @Test
    public void assertGetZookeeperConfiguration() throws NoSuchFieldException {
        Properties properties = new Properties();
        properties.setProperty(EnvironmentArgument.ZOOKEEPER_DIGEST.getKey(), "test");
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        ZookeeperConfiguration zkConfig = bootstrapEnvironment.getZookeeperConfiguration();
        assertThat(zkConfig.getServerLists(), is("localhost:2181"));
        assertThat(zkConfig.getNamespace(), is("elastic-job-cloud"));
        assertThat(zkConfig.getDigest(), is("test"));
    }
    
    @Test
    public void assertGetRestfulServerConfiguration() {
        RestfulServerConfiguration restfulServerConfig = bootstrapEnvironment.getRestfulServerConfiguration();
        assertThat(restfulServerConfig.getPort(), is(8899));
    }
}
