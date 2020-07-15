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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.env;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class BootstrapEnvironmentTest {
    
    private final BootstrapEnvironment bootstrapEnvironment = BootstrapEnvironment.getInstance();
    
    @Test
    public void assertGetMesosConfiguration() {
        MesosConfiguration mesosConfig = bootstrapEnvironment.getMesosConfiguration();
        assertThat(mesosConfig.getHostname(), is("localhost"));
        assertThat(mesosConfig.getUser(), is(""));
        assertThat(mesosConfig.getUrl(), is("zk://localhost:2181/mesos"));
    }
    
    @Test
    public void assertGetZookeeperConfiguration() {
        Properties properties = new Properties();
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.ZOOKEEPER_DIGEST.getKey(), "test");
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        ZookeeperConfiguration zkConfig = bootstrapEnvironment.getZookeeperConfiguration();
        assertThat(zkConfig.getServerLists(), is("localhost:2181"));
        assertThat(zkConfig.getNamespace(), is("elasticjob-cloud"));
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
    public void assertGetEventTraceRdbConfiguration() {
        Properties properties = new Properties();
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_DRIVER.getKey(), "org.h2.Driver");
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_URL.getKey(), "jdbc:h2:mem:job_event_trace");
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_USERNAME.getKey(), "sa");
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_PASSWORD.getKey(), "password");
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        Optional<TracingConfiguration> tracingConfiguration = bootstrapEnvironment.getTracingConfiguration();
        tracingConfiguration.ifPresent(tracingConfiguration1 -> assertThat(tracingConfiguration1.getStorage(), instanceOf(BasicDataSource.class)));
    }
    
    @Test
    public void assertWithoutEventTraceRdbConfiguration() {
        assertFalse(bootstrapEnvironment.getTracingConfiguration().isPresent());
    }
    
    @Test
    public void assertGetEventTraceRdbConfigurationMap() {
        Properties properties = new Properties();
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_DRIVER.getKey(), "org.h2.Driver");
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_URL.getKey(), "jdbc:h2:mem:job_event_trace");
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_USERNAME.getKey(), "sa");
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_PASSWORD.getKey(), "password");
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        Map<String, String> jobEventRdbConfigurationMap = bootstrapEnvironment.getJobEventRdbConfigurationMap();
        assertThat(jobEventRdbConfigurationMap.get(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_DRIVER.getKey()), is("org.h2.Driver"));
        assertThat(jobEventRdbConfigurationMap.get(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_URL.getKey()), is("jdbc:h2:mem:job_event_trace"));
        assertThat(jobEventRdbConfigurationMap.get(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_USERNAME.getKey()), is("sa"));
        assertThat(jobEventRdbConfigurationMap.get(BootstrapEnvironment.EnvironmentArgument.EVENT_TRACE_RDB_PASSWORD.getKey()), is("password"));
    }
    
    @Test
    public void assertReconcileConfiguration() {
        FrameworkConfiguration configuration = bootstrapEnvironment.getFrameworkConfiguration();
        assertThat(configuration.getReconcileIntervalMinutes(), is(-1));
        assertFalse(configuration.isEnabledReconcile());
        Properties properties = new Properties();
        properties.setProperty(BootstrapEnvironment.EnvironmentArgument.RECONCILE_INTERVAL_MINUTES.getKey(), "0");
        ReflectionUtils.setFieldValue(bootstrapEnvironment, "properties", properties);
        configuration = bootstrapEnvironment.getFrameworkConfiguration();
        assertThat(configuration.getReconcileIntervalMinutes(), is(0));
        assertFalse(configuration.isEnabledReconcile());
    }
}
