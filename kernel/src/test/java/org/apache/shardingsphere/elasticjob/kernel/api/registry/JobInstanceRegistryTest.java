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

package org.apache.shardingsphere.elasticjob.kernel.api.registry;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JobInstanceRegistryTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Test
    void assertListenWithoutConfigPath() {
        new JobInstanceRegistry(regCenter, new JobInstance("id")).new JobInstanceRegistryListener().onChange(new DataChangedEvent(Type.ADDED, "/jobName", ""));
        verify(regCenter, times(0)).get("/jobName");
    }
    
    @Test
    void assertListenLabelNotMatch() {
        String jobConfig = toYaml(JobConfiguration.newBuilder("jobName", 1).label("label").build());
        new JobInstanceRegistry(regCenter, new JobInstance("id", "label1,label2")).new JobInstanceRegistryListener().onChange(new DataChangedEvent(Type.ADDED, "/jobName/config", jobConfig));
        verify(regCenter, times(0)).get("/jobName");
    }
    
    @Test
    void assertListenScheduleJob() {
        assertThrows(RuntimeException.class, () -> {
            String jobConfig = toYaml(JobConfiguration.newBuilder("jobName", 1).cron("0/1 * * * * ?").label("label").build());
            new JobInstanceRegistry(regCenter, new JobInstance("id")).new JobInstanceRegistryListener().onChange(new DataChangedEvent(Type.ADDED, "/jobName/config", jobConfig));
        });
    }
    
    @Test
    void assertListenOneOffJob() {
        assertThrows(RuntimeException.class, () -> {
            String jobConfig = toYaml(JobConfiguration.newBuilder("jobName", 1).label("label").build());
            new JobInstanceRegistry(regCenter, new JobInstance("id", "label")).new JobInstanceRegistryListener().onChange(new DataChangedEvent(Type.ADDED, "/jobName/config", jobConfig));
        });
    }
    
    private String toYaml(final JobConfiguration build) {
        return YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(build));
    }
}
