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

package com.dangdang.ddframe.job.lite.lifecycle.internal.operate;

import com.dangdang.ddframe.job.lite.lifecycle.api.ShardingOperateAPI;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShardingOperateAPIImplTest {
    
    private ShardingOperateAPI shardingOperateAPI;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        shardingOperateAPI = new ShardingOperateAPIImpl(regCenter);
    }
    
    @Test
    public void assertDisableSharding() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.getChildrenKeys("/test_job/sharding")).thenReturn(Arrays.asList("0", "1"));
        shardingOperateAPI.disable("test_job", "0");
        verify(regCenter).persist("/test_job/sharding/0/disabled", "");
    }

    @Test
    public void assertEnableSharding() {
        when(regCenter.getChildrenKeys("/test_job/servers")).thenReturn(Arrays.asList("ip1", "ip2"));
        when(regCenter.getChildrenKeys("/test_job/sharding")).thenReturn(Arrays.asList("0", "1"));
        shardingOperateAPI.enable("test_job", "0");
        verify(regCenter).remove("/test_job/sharding/0/disabled");
    }

}
