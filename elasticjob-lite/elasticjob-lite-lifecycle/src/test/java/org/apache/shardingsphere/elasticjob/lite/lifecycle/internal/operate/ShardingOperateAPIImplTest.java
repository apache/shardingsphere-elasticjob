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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate;

import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.ShardingOperateAPI;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingOperateAPIImplTest {
    
    private ShardingOperateAPI shardingOperateAPI;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() {
        shardingOperateAPI = new ShardingOperateAPIImpl(regCenter);
    }
    
    @Test
    public void assertDisableSharding() {
        shardingOperateAPI.disable("test_job", "0");
        verify(regCenter).persist("/test_job/sharding/0/disabled", "");
    }

    @Test
    public void assertEnableSharding() {
        shardingOperateAPI.enable("test_job", "0");
        verify(regCenter).remove("/test_job/sharding/0/disabled");
    }

}
