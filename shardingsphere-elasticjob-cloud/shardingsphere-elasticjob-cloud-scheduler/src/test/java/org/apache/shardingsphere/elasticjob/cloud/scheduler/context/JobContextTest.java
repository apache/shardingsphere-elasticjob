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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.context;

import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class JobContextTest {
    
    @Test
    public void assertFrom() {
        CloudJobConfiguration cloudJobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job").toCloudJobConfiguration();
        JobContext actual = JobContext.from(cloudJobConfig, ExecutionType.READY);
        assertThat(actual.getAssignedShardingItems().size(), is(10));
        for (int i = 0; i < actual.getAssignedShardingItems().size(); i++) {
            assertThat(actual.getAssignedShardingItems().get(i), is(i));
        }
    }
}
