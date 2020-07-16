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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.shardingsphere.elasticjob.api.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class TaskInfoDataTest {
    
    private final ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 3, "test_param", Collections.emptyMap());
    
    @Test
    public void assertSerializeSimpleJob() {
        TaskInfoData actual = new TaskInfoData(shardingContexts, CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job").toCloudJobConfiguration());
        assertSerialize(SerializationUtils.deserialize(actual.serialize()));
    }
    
    @Test
    public void assertSerializeDataflowJob() {
        TaskInfoData actual = new TaskInfoData(shardingContexts, CloudJobConfigurationBuilder.createDataflowCloudJobConfiguration("test_job"));
        assertSerialize(SerializationUtils.deserialize(actual.serialize()));
    }
    
    @Test
    public void assertSerializeScriptJob() {
        TaskInfoData actual = new TaskInfoData(shardingContexts, CloudJobConfigurationBuilder.createScriptCloudJobConfiguration("test_job").toCloudJobConfiguration());
        assertSerialize(SerializationUtils.deserialize(actual.serialize()));
    }
    
    private void assertSerialize(final Map expected) {
        assertThat(expected.size(), is(2));
        assertNotNull(expected.get("shardingContext"));
        assertNotNull(expected.get("jobConfigContext"));
    }
}
