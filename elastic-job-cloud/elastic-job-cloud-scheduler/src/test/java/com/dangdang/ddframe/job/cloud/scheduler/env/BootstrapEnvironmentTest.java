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

package com.dangdang.ddframe.job.cloud.scheduler.env;

import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment.EnvironmentArgument;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.google.common.base.Optional;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.unitils.util.ReflectionUtils;

import java.util.Map;
import java.util.Properties;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public final class BootstrapEnvironmentTest {
    
    private final BootstrapEnvironment bootstrapEnvironment = BootstrapEnvironment.getInstance();
    
    @Test
    public void assertGetMesosConfiguration() throws NoSuchFieldException {
        MesosConfiguration mesosConfig = bootstrapEnvironment.getMesosConfiguration();
        assertThat(mesosConfig.getHostname(), is("localhost"));
        assertThat(mesosConfig.getUser(), is(""));
        assertThat(mesosConfig.getUrl(), is("zk://localhost:2181/mesos"));
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
    
    @Test
    public void assertGetFrameworkConfiguration() {
        FrameworkConfiguration frameworkConfig = bootstrapEnvironment.getFrameworkConfiguration();
        assertThat(frameworkConfig.getJobStateQueueSize(), is(10000));
    }
    
    @Test
    public void assertGetEventTraceRdbConfiguration() throws NoSuchFieldException {
        Properties properties = new Properties();
        properties.setProperty(EnvironmentArgument.EVENT_TRACE_RDB_DRIVER.getKey(), "org.h2.Driver");
        properties.setProperty(EnvironmentArgument.EVENT_TRACE_RDB_URL.getKey(), "jdbc:h2:mem:job_event_trace");
        properties.setProperty(EnvironmentArgument.EVENT_TRACE_RDB_USERNAME.getKey(), "sa");
        properties.setProperty(EnvironmentArgument.EVENT_TRACE_RDB_PASSWORD.getKey(), "password");
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        Optional<JobEventRdbConfiguration> jobEventRdbConfiguration = bootstrapEnvironment.getJobEventRdbConfiguration();
        if (jobEventRdbConfiguration.isPresent()) {
            assertThat(jobEventRdbConfiguration.get().getDataSource(), instanceOf(BasicDataSource.class));
        }
    }
    
    @Test
    public void assertWithoutEventTraceRdbConfiguration() throws NoSuchFieldException {
        assertFalse(bootstrapEnvironment.getJobEventRdbConfiguration().isPresent());
    }
    
    @Test
    public void assertGetEventTraceRdbConfigurationMap() throws NoSuchFieldException {
        Properties properties = new Properties();
        properties.setProperty(EnvironmentArgument.EVENT_TRACE_RDB_DRIVER.getKey(), "org.h2.Driver");
        properties.setProperty(EnvironmentArgument.EVENT_TRACE_RDB_URL.getKey(), "jdbc:h2:mem:job_event_trace");
        properties.setProperty(EnvironmentArgument.EVENT_TRACE_RDB_USERNAME.getKey(), "sa");
        properties.setProperty(EnvironmentArgument.EVENT_TRACE_RDB_PASSWORD.getKey(), "password");
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        Map<String, String> jobEventRdbConfigurationMap = bootstrapEnvironment.getJobEventRdbConfigurationMap();
        assertThat(jobEventRdbConfigurationMap.get(EnvironmentArgument.EVENT_TRACE_RDB_DRIVER.getKey()), is("org.h2.Driver"));
        assertThat(jobEventRdbConfigurationMap.get(EnvironmentArgument.EVENT_TRACE_RDB_URL.getKey()), is("jdbc:h2:mem:job_event_trace"));
        assertThat(jobEventRdbConfigurationMap.get(EnvironmentArgument.EVENT_TRACE_RDB_USERNAME.getKey()), is("sa"));
        assertThat(jobEventRdbConfigurationMap.get(EnvironmentArgument.EVENT_TRACE_RDB_PASSWORD.getKey()), is("password"));
    }
}
