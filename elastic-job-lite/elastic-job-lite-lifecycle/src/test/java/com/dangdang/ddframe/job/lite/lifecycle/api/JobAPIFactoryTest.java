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

package com.dangdang.ddframe.job.lite.lifecycle.api;

import com.dangdang.ddframe.job.lite.lifecycle.AbstractEmbedZookeeperBaseTest;
import com.google.common.base.Optional;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class JobAPIFactoryTest extends AbstractEmbedZookeeperBaseTest {
    
    @Test
    public void assertCreateJobSettingsAPI() {
        assertThat(JobAPIFactory.createJobSettingsAPI(getConnectionString(), "namespace", Optional.<String>absent()), instanceOf(JobSettingsAPI.class));
    }
    
    @Test
    public void assertCreateJobOperateAPI() {
        assertThat(JobAPIFactory.createJobOperateAPI(getConnectionString(), "namespace", Optional.<String>absent()), instanceOf(JobOperateAPI.class));
    }
    
    @Test
    public void assertCreateServerOperateAPI() {
        assertThat(JobAPIFactory.createShardingOperateAPI(getConnectionString(), "namespace", Optional.<String>absent()), instanceOf(ShardingOperateAPI.class));
    }
    
    @Test
    public void assertCreateJobStatisticsAPI() {
        assertThat(JobAPIFactory.createJobStatisticsAPI(getConnectionString(), "namespace", Optional.<String>absent()), instanceOf(JobStatisticsAPI.class));
    }
    
    @Test
    public void assertCreateServerStatisticsAPI() {
        assertThat(JobAPIFactory.createServerStatisticsAPI(getConnectionString(), "namespace", Optional.<String>absent()), instanceOf(ServerStatisticsAPI.class));
    }
    
    @Test
    public void assertCreateShardingStatisticsAPI() {
        assertThat(JobAPIFactory.createShardingStatisticsAPI(getConnectionString(), "namespace", Optional.<String>absent()), instanceOf(ShardingStatisticsAPI.class));
    }
}
