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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.statistics;

import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.ShardingStatisticsAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.ShardingInfo;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingStatisticsAPIImplTest {
    
    private ShardingStatisticsAPI shardingStatisticsAPI;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() {
        shardingStatisticsAPI = new ShardingStatisticsAPIImpl(regCenter);
    }
    
    @Test
    public void assertGetShardingInfo() {
        when(regCenter.getChildrenKeys("/test_job/sharding")).thenReturn(Arrays.asList("0", "1", "2", "3"));
        when(regCenter.get("/test_job/sharding/0/instance")).thenReturn("ip1@-@1234");
        when(regCenter.get("/test_job/sharding/1/instance")).thenReturn("ip2@-@2341");
        when(regCenter.get("/test_job/sharding/2/instance")).thenReturn("ip3@-@3412");
        when(regCenter.get("/test_job/sharding/3/instance")).thenReturn("ip4@-@4123");
        when(regCenter.isExisted("/test_job/instances/ip4@-@4123")).thenReturn(true);
        when(regCenter.isExisted("/test_job/sharding/0/running")).thenReturn(true);
        when(regCenter.isExisted("/test_job/sharding/1/running")).thenReturn(false);
        when(regCenter.isExisted("/test_job/sharding/2/running")).thenReturn(false);
        when(regCenter.isExisted("/test_job/sharding/3/running")).thenReturn(false);
        when(regCenter.isExisted("/test_job/sharding/0/failover")).thenReturn(false);
        when(regCenter.isExisted("/test_job/sharding/1/failover")).thenReturn(true);
        when(regCenter.isExisted("/test_job/sharding/2/disabled")).thenReturn(true);
        int i = 0;
        for (ShardingInfo each : shardingStatisticsAPI.getShardingInfo("test_job")) {
            i++;
            assertThat(each.getItem(), is(i - 1));
            switch (i) {
                case 1:
                    assertThat(each.getStatus(), is(ShardingInfo.ShardingStatus.RUNNING));
                    assertThat(each.getServerIp(), is("ip1"));
                    assertThat(each.getInstanceId(), is("1234"));
                    break;
                case 2:
                    assertTrue(each.isFailover());
                    assertThat(each.getStatus(), is(ShardingInfo.ShardingStatus.SHARDING_FLAG));
                    assertThat(each.getServerIp(), is("ip2"));
                    assertThat(each.getInstanceId(), is("2341"));
                    break;
                case 3:
                    assertThat(each.getStatus(), is(ShardingInfo.ShardingStatus.DISABLED));
                    assertThat(each.getServerIp(), is("ip3"));
                    assertThat(each.getInstanceId(), is("3412"));
                    break;
                case 4:
                    assertThat(each.getStatus(), is(ShardingInfo.ShardingStatus.PENDING));
                    assertThat(each.getServerIp(), is("ip4"));
                    assertThat(each.getInstanceId(), is("4123"));
                    break;
                default:
                    break;
            }
        }
    }
}
