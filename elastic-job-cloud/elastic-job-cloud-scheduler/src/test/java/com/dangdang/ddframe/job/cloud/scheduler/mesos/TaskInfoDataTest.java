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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class TaskInfoDataTest {
    
    private final ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 3, "test_param", Collections.<Integer, String>emptyMap());
    
    @Test
    public void assertSerializeSimpleJob() {
        TaskInfoData actual = new TaskInfoData(shardingContexts, CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job"));
        assertSerialize((Map) SerializationUtils.deserialize(actual.serialize()));
    }
    
    @Test
    public void assertSerializeDataflowJob() {
        TaskInfoData actual = new TaskInfoData(shardingContexts, CloudJobConfigurationBuilder.createDataflowCloudJobConfiguration("test_job"));
        assertSerialize((Map) SerializationUtils.deserialize(actual.serialize()));
    }
    
    @Test
    public void assertSerializeScriptJob() {
        TaskInfoData actual = new TaskInfoData(shardingContexts, CloudJobConfigurationBuilder.createScriptCloudJobConfiguration("test_job"));
        assertSerialize((Map) SerializationUtils.deserialize(actual.serialize()));
    }
    
    private void assertSerialize(final Map expected) {
        assertThat(expected.size(), is(2));
        assertNotNull(expected.get("shardingContext"));
        assertNotNull(expected.get("jobConfigContext"));
    }
}
