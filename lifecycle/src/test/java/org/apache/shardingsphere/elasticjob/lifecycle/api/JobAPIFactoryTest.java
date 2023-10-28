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

package org.apache.shardingsphere.elasticjob.lifecycle.api;

import org.apache.shardingsphere.elasticjob.test.util.EmbedTestingServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

class JobAPIFactoryTest {
    
    private static final EmbedTestingServer EMBED_TESTING_SERVER = new EmbedTestingServer(8181);
    
    @BeforeAll
    static void setUp() {
        EMBED_TESTING_SERVER.start();
    }
    
    @Test
    void assertCreateJobConfigAPI() {
        assertThat(JobAPIFactory.createJobConfigurationAPI(EMBED_TESTING_SERVER.getConnectionString(), "namespace", null), instanceOf(JobConfigurationAPI.class));
    }
    
    @Test
    void assertCreateJobOperateAPI() {
        assertThat(JobAPIFactory.createJobOperateAPI(EMBED_TESTING_SERVER.getConnectionString(), "namespace", null), instanceOf(JobOperateAPI.class));
    }
    
    @Test
    void assertCreateServerOperateAPI() {
        assertThat(JobAPIFactory.createShardingOperateAPI(EMBED_TESTING_SERVER.getConnectionString(), "namespace", null), instanceOf(ShardingOperateAPI.class));
    }
    
    @Test
    void assertCreateJobStatisticsAPI() {
        assertThat(JobAPIFactory.createJobStatisticsAPI(EMBED_TESTING_SERVER.getConnectionString(), "namespace", null), instanceOf(JobStatisticsAPI.class));
    }
    
    @Test
    void assertCreateServerStatisticsAPI() {
        assertThat(JobAPIFactory.createServerStatisticsAPI(EMBED_TESTING_SERVER.getConnectionString(), "namespace", null), instanceOf(ServerStatisticsAPI.class));
    }
    
    @Test
    void assertCreateShardingStatisticsAPI() {
        assertThat(JobAPIFactory.createShardingStatisticsAPI(EMBED_TESTING_SERVER.getConnectionString(), "namespace", null), instanceOf(ShardingStatisticsAPI.class));
    }
}
