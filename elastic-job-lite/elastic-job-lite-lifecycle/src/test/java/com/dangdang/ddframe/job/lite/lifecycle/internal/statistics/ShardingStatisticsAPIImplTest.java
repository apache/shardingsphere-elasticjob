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

package com.dangdang.ddframe.job.lite.lifecycle.internal.statistics;

import com.dangdang.ddframe.job.lite.lifecycle.api.ShardingStatisticsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ShardingInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ShardingInfo.ShardingStatus;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public final class ShardingStatisticsAPIImplTest {
    
    private ShardingStatisticsAPI shardingStatisticsAPI;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        shardingStatisticsAPI = new ShardingStatisticsAPIImpl(regCenter);
    }
    
    @Test
    public void assertGetShardingInfo() {
        when(regCenter.isExisted("/test_job/sharding")).thenReturn(true);
        when(regCenter.getChildrenKeys("/test_job/sharding")).thenReturn(Arrays.asList("0", "1", "2"));
        when(regCenter.isExisted("/test_job/sharding/0/running")).thenReturn(true);
        when(regCenter.get("/test_job/sharding/0/instance")).thenReturn("ip1@-@defaultInstance");
        when(regCenter.get("/test_job/sharding/1/instance")).thenReturn("ip2@-@defaultInstance");
        when(regCenter.get("/test_job/sharding/2/instance")).thenReturn("ip3@-@defaultInstance");
        when(regCenter.isExisted("/test_job/sharding/1/running")).thenReturn(false);
        when(regCenter.isExisted("/test_job/sharding/1/completed")).thenReturn(true);
        when(regCenter.isExisted("/test_job/sharding/2/running")).thenReturn(false);
        when(regCenter.isExisted("/test_job/sharding/2/completed")).thenReturn(false);
        when(regCenter.isExisted("/test_job/sharding/0/failover")).thenReturn(false);
        when(regCenter.isExisted("/test_job/sharding/1/failover")).thenReturn(false);
        when(regCenter.isExisted("/test_job/sharding/2/failover")).thenReturn(true);
        int i = 0;
        for (ShardingInfo each : shardingStatisticsAPI.getShardingInfo("test_job")) {
            i++;
            assertThat(each.getItem(), is(i - 1));
            switch (i) {
                case 1:
                    assertFalse(each.isFailover());
                    assertThat(each.getStatus(), is(ShardingInfo.ShardingStatus.RUNNING));
                    assertThat(each.getServerIp(), is("ip1"));
                    break;
                case 2:
                    assertFalse(each.isFailover());
                    assertThat(each.getStatus(), is(ShardingInfo.ShardingStatus.COMPLETED));
                    assertThat(each.getServerIp(), is("ip2"));
                    break;
                case 3:
                    assertTrue(each.isFailover());
                    assertThat(each.getStatus(), is(ShardingStatus.SHARDING_ERROR));
                    assertThat(each.getServerIp(), is("ip3"));
                    break;
                default:
                    break;
            }
        }
    }
}
