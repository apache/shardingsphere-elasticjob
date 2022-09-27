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

package org.apache.shardingsphere.elasticjob.lite.api.registry;

import org.apache.shardingsphere.elasticjob.infra.listener.CuratorCacheListener;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class JobInstanceRegistryTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Test
    public void assertListenWithoutConfigPath() {
        JobInstanceRegistry jobInstanceRegistry = new JobInstanceRegistry(regCenter, new JobInstance("id"));
        jobInstanceRegistry.new JobInstanceRegistryListener().dataChanged("/jobName", CuratorCacheListener.Type.NODE_CREATED, "");
        verify(regCenter, times(0)).get("/jobName");
    }
    
    @Test
    public void assertListenLabelNotMatch() {
        JobInstanceRegistry jobInstanceRegistry = new JobInstanceRegistry(regCenter, new JobInstance("id", "label1,label2"));
        String jobConfig = toYaml(JobConfiguration.newBuilder("jobName", 1).label("label").build());
        jobInstanceRegistry.new JobInstanceRegistryListener().dataChanged("/jobName/config", CuratorCacheListener.Type.NODE_CREATED, jobConfig);
        verify(regCenter, times(0)).get("/jobName");
    }
    
    @Test(expected = RuntimeException.class)
    public void assertListenScheduleJob() {
        JobInstanceRegistry jobInstanceRegistry = new JobInstanceRegistry(regCenter, new JobInstance("id"));
        String jobConfig = toYaml(JobConfiguration.newBuilder("jobName", 1).cron("0/1 * * * * ?").label("label").build());
        jobInstanceRegistry.new JobInstanceRegistryListener().dataChanged("/jobName/config", CuratorCacheListener.Type.NODE_CREATED, jobConfig);
    }
    
    @Test(expected = RuntimeException.class)
    public void assertListenOneOffJob() {
        JobInstanceRegistry jobInstanceRegistry = new JobInstanceRegistry(regCenter, new JobInstance("id", "label"));
        String jobConfig = toYaml(JobConfiguration.newBuilder("jobName", 1).label("label").build());
        jobInstanceRegistry.new JobInstanceRegistryListener().dataChanged("/jobName/config", CuratorCacheListener.Type.NODE_CREATED, jobConfig);
    }
    
    private String toYaml(final JobConfiguration build) {
        return YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(build));
    }
}
